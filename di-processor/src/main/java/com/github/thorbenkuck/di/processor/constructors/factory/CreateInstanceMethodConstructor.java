package com.github.thorbenkuck.di.processor.constructors.factory;

import com.github.thorbenkuck.di.ReflectionsHelper;
import com.github.thorbenkuck.di.domain.WireRepository;
import com.github.thorbenkuck.di.annotations.Nullable;
import com.github.thorbenkuck.di.processor.FetchAnnotated;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.constructors.MethodConstructor;
import com.github.thorbenkuck.di.processor.foundation.Logger;
import com.github.thorbenkuck.di.processor.foundation.ProcessingException;
import com.github.thorbenkuck.di.processor.foundation.ProcessorContext;
import com.squareup.javapoet.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class CreateInstanceMethodConstructor implements MethodConstructor {

    public static final String METHOD_NAME = "createInstance";
    private static final String INTERMITTENT_VARIABLE_NAME = "instance";

    @Override
    public final String methodName() {
        return METHOD_NAME;
    }

    @Override
    public final void construct(WireInformation wireInformation, TypeSpec.Builder typeBuilder) {
        TypeElement typeElement = wireInformation.getPrimaryWireType();
        MethodSpec.Builder method = privateMethod()
                .addParameter(WireRepository.class, "wiredTypes")
                .returns(TypeName.get(typeElement.asType()));

        CodeBlock.Builder codeBodyBuilder = CodeBlock.builder();
        buildConstructorBlock(wireInformation, codeBodyBuilder);

        InjectionContext injectionContext = new InjectionContext();
        findFieldInjections(typeElement, injectionContext);
        applyInjectionContext(typeElement, injectionContext, codeBodyBuilder);
        tryApplyPostConstructMethods(typeElement, codeBodyBuilder);

        appendPreReturn(codeBodyBuilder);
        codeBodyBuilder.add("\n// And we are done! A fresh and newly created instance, ready for you! \n")
                .addStatement("return $L", INTERMITTENT_VARIABLE_NAME);
        appendPostReturn(codeBodyBuilder);

        method.addCode(codeBodyBuilder.build());
        typeBuilder.addMethod(method.build());
    }

    protected abstract List<String> findAndApplyConstructorParameters(
            CodeBlock.Builder codeBlockBuilder,
            List<? extends VariableElement> typeElement,
            WireInformation wireInformation
    );

    protected abstract void findFieldInjections(TypeElement typeElement, InjectionContext injectionContext);

    protected void appendPreReturn(CodeBlock.Builder builder) {
        // Default: Do nothing
    }

    protected void appendPostReturn(CodeBlock.Builder builder) {

    }

    private void applyInjectionContext(TypeElement typeElement, InjectionContext context, CodeBlock.Builder builder) {
        applyFieldInjections(typeElement, context.fieldInjections, builder);
        applySetterInjections(context.setterInjections, builder);
    }

    private List<ExecutableElement> findPostConstructMethods(TypeElement typeElement) {
        List<ExecutableElement> postConstructs = new ArrayList<>();
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD && element.getAnnotation(PostConstruct.class) != null) {
                postConstructs.add((ExecutableElement) element);
            }
        }

        return postConstructs;
    }

    private void tryApplyPostConstructMethods(TypeElement typeElement, CodeBlock.Builder codeBlockBuilder) {
        List<ExecutableElement> postConstructMethods = findPostConstructMethods(typeElement);

        if (postConstructMethods.isEmpty()) {
            return;
        }

        codeBlockBuilder.add("\n// Now post construct invocations\n");
        postConstructMethods.forEach(postConstruct -> {
            if (!postConstruct.getParameters().isEmpty()) {
                throw new ProcessingException(typeElement, "No Arguments allowed on methods annotated with @PostConstruction");
            }

            if (postConstruct.getModifiers().contains(Modifier.PRIVATE)) {
                Logger.warn("It is highly discouraged to try and invoke private method. Maybe make it protected?", postConstruct);
                codeBlockBuilder.addStatement("$T.invokeMethod(instance, $T.class, $S, $T.class)", ReflectionsHelper.class, ClassName.get(typeElement), postConstruct.getSimpleName(), ClassName.get(postConstruct.getReturnType()));
            } else {
                codeBlockBuilder.addStatement("instance.$L()", postConstruct.getSimpleName());
            }
        });
    }

    private void applyFieldInjections(TypeElement typeElement, List<InjectionContext.Field> fields, CodeBlock.Builder builder) {
        if(!fields.isEmpty()) {
            builder.add("\n// Do all the field injections \n");
            Logger.warn("It is highly discouraged to rely on field injection. Prefer constructor injection if possible", typeElement);
        }
        fields.forEach(field -> {
            ClassName className = ClassName.get(field.type);
            CodeBlock.Builder fetchValueBlock = CodeBlock.builder();
            if (field.isProperty) {
                if(field.propertyDefaultValue == null) {
                    fetchValueBlock.add("wiredTypes.properties().getTyped($S, $T.class)", field.name, field.type);
                } else {
                    fetchValueBlock.add("wiredTypes.properties().getTyped($S, $T.class, $S)", field.name, field.type, field.propertyDefaultValue);
                }
            } else {
                if (field.variableElement.getAnnotation(Nullable.class) == null) {
                    fetchValueBlock.add("wiredTypes.requireInstance($T.class)", className);
                } else {
                    fetchValueBlock.add("wiredTypes.tryGetInstance($T.class)", className);
                }
            }

            builder.addStatement("$T.setField($S, $L, $T.class, $L)", ClassName.get(ReflectionsHelper.class), field.name, INTERMITTENT_VARIABLE_NAME, ClassName.get(typeElement), fetchValueBlock.build());
        });
    }

    private void applySetterInjections(List<InjectionContext.Setter> setters, CodeBlock.Builder builder) {
        if(!setters.isEmpty()) {
            builder.add("\n// Do all the setter injections \n");
        }
        setters.forEach(field -> {
            ClassName className = ClassName.get(field.type);
            if (field.type.getAnnotation(Nullable.class) != null) {
                builder.add("$L.$L(wiredTypes.getInstance($T))", INTERMITTENT_VARIABLE_NAME, field.methodName, className);
            } else {
                builder.add("$T.setField(wiredTypes.requireInstance($T))", INTERMITTENT_VARIABLE_NAME, field.methodName, className);
            }
        });
    }

    private void buildConstructorBlock(WireInformation wireInformation, CodeBlock.Builder codeBodyBuilder) {
        ClassName className = wireInformation.realClassName();
        Optional<ExecutableElement> potentialPrimaryConstructor = wireInformation.getPrimaryConstructor();
        if (!potentialPrimaryConstructor.isPresent()) {
            codeBodyBuilder
                    .add("// No Parameters, just create a new instance \n")
                    .addStatement("$T $L = new $T()", className, INTERMITTENT_VARIABLE_NAME, className);
        } else {
            ExecutableElement constructor = potentialPrimaryConstructor.get();
            if (constructor.getParameters().isEmpty()) {
                codeBodyBuilder
                        .add("// No Parameters, just create a new instance \n")
                        .addStatement("$T $L = new $T()", className, INTERMITTENT_VARIABLE_NAME, className);
            } else {
                codeBodyBuilder.add("// Get all the required parameters for constructor Injection\n");
                List<String> names = findAndApplyConstructorParameters(codeBodyBuilder, constructor.getParameters(), wireInformation);

                StringBuilder argumentList = new StringBuilder(names.get(0));
                for (int count = 1; count < names.size(); count++) {
                    argumentList.append(", ").append(names.get(count));
                }

                codeBodyBuilder
                        .add("\n// Now, create the  actual instance\n")
                        .addStatement("$T $L = new $T($L)", className, INTERMITTENT_VARIABLE_NAME, className, argumentList.toString());
            }
        }
    }

    static class InjectionContext {

        private final List<Field> fieldInjections = new ArrayList<>();
        private final List<Setter> setterInjections = new ArrayList<>();

        public void announceFieldInjection(VariableElement variableElement, boolean isProperty, String propertyDefaultValue) {
            fieldInjections.add(new Field(variableElement.getSimpleName().toString(), variableElement, isProperty, propertyDefaultValue));
        }

        public void announceFieldInjection(VariableElement variableElement) {
            announceFieldInjection(variableElement, false, null);
        }

        public void announceSetterInjection(String name, TypeElement type, boolean isProperty, String propertyDefaultValue) {
            setterInjections.add(new Setter(name, type, isProperty, propertyDefaultValue));
        }

        public void announceSetterInjection(String name, TypeElement type) {
            announceSetterInjection(name, type, false, null);
        }

        private static String convertedDefault(String input) {
            if(input == null) {
                return null;
            }

            if(input.isEmpty()) {
                return null;
            }

            return input;
        }

        public class Field {
            private final String name;
            private final VariableElement variableElement;
            private final TypeElement type;
            private final boolean isProperty;
            private final String propertyDefaultValue;

            public Field(String name, VariableElement variableElement, boolean isProperty, String propertyDefaultValue) {
                this.name = name;
                this.variableElement = variableElement;
                this.type = (TypeElement) ProcessorContext.getTypes().asElement(variableElement.asType());
                this.isProperty = isProperty;
                this.propertyDefaultValue = convertedDefault(propertyDefaultValue);
            }

            public VariableElement getVariableElement() {
                return variableElement;
            }
        }

        public class Setter {
            private final String methodName;
            private final TypeElement type;
            private final boolean isProperty;
            private final String propertyDefaultValue;

            public Setter(String methodName, TypeElement type, boolean isProperty, String propertyDefaultValue) {
                this.methodName = methodName;
                this.type = type;
                this.isProperty = isProperty;
                this.propertyDefaultValue = convertedDefault(propertyDefaultValue);
            }
        }
    }
}
