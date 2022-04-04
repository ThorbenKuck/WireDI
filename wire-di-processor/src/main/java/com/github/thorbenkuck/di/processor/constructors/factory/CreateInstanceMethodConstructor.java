package com.github.thorbenkuck.di.processor.constructors.factory;

import com.github.thorbenkuck.di.ReflectionsHelper;
import com.github.thorbenkuck.di.WireRepository;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.constructors.MethodConstructor;
import com.github.thorbenkuck.di.processor.Logger;
import com.squareup.javapoet.*;

import javax.annotation.PostConstruct;
import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class CreateInstanceMethodConstructor implements MethodConstructor {

    public static final String METHOD_NAME = "createInstance";
    private static final String instanceName = "instance";

    @Override
    public final String methodName() {
        return METHOD_NAME;
    }

    protected abstract VariableName fetchConstructorVariable(InjectionContext.ConstructorParameter constructorParameter, CodeBlock.Builder builder);

    protected abstract CodeBlock fetchVariableForInjection(TypeElement typeElement, boolean nullable);

    private void addConstructorInvocations(InjectionContext injectionContext, CodeBlock.Builder methodBodyBuilder, WireInformation wireInformation) {
        methodBodyBuilder.add("// Get all the required constructor parameters\n");
        List<VariableName> variableNames = new ArrayList<>();
        injectionContext.drainConstructorParameters()
                .forEach(constructorParameter -> {
                    VariableName variableName = fetchConstructorVariable(constructorParameter, methodBodyBuilder);
                    Objects.requireNonNull(variableName, "The parameter " + constructorParameter.getName() + " must not be null");
                    variableNames.add(variableName);
                });

        Logger.debug("Constructor of " + wireInformation.getPrimaryWireType().getSimpleName() + " has " + variableNames.size() + " arguments: " + variableNames);
        String arguments = variableNames.stream()
                .map(it -> it.value)
                .collect(Collectors.joining(", "));

        methodBodyBuilder.add("\n// Now, lets create the  actual instance\n");
        if (injectionContext.hasNoFurtherInjectionQualities()) {
            injectionContext.markAsReturned();
            methodBodyBuilder.addStatement("return new $T($L)", wireInformation.realClassName(), arguments);
        } else {
            methodBodyBuilder.addStatement("$T $L = new $T($L)", wireInformation.primaryClassName(), instanceName, wireInformation.realClassName(), arguments);
        }
    }

    private void addSetterInjections(InjectionContext injectionContext, CodeBlock.Builder methodBodyBuilder, WireInformation wireInformation) {
        if (injectionContext.hasNoSetterInjections()) {
            return;
        }

        List<InjectionContext.Setter> setters = injectionContext.drainSetterInjections();
        methodBodyBuilder.add("\n// Some wild setter injections appeared!\n")
                .add("// If you are reading this, consider using something else. Protected or package private method signatures maybe? Or injecting it into the constructor?\n");

        for (InjectionContext.Setter setter : setters) {
            CodeBlock fetchVariable = fetchVariableForInjection(setter.getType(), setter.mayBeNull());
            if (setter.requiresReflection()) {
                Logger.reflectionWarning(setter.getExecutableElement());
                ClassName invokeUpon = ClassName.get(ReflectionsHelper.class);
                ClassName invokeOn = wireInformation.primaryClassName();
                Name name = setter.getMethodName();

                methodBodyBuilder.addStatement("$T.invokeMethod(instance, $T.class, $S, $T.class, $L)",
                        invokeUpon,
                        invokeOn,
                        name,
                        ClassName.get(setter.getExecutableElement().getReturnType()),
                        fetchVariable
                );
            } else {
                methodBodyBuilder.addStatement("$L.$L($L)", instanceName, setter.getMethodName(), fetchVariable);
            }
        }
    }

    private void addFieldsInjections(InjectionContext injectionContext, CodeBlock.Builder methodBodyBuilder, WireInformation wireInformation) {
        if (injectionContext.hasNoFieldInjections()) {
            return;
        }

        List<InjectionContext.Field> fields = injectionContext.drainFieldInjections();
        methodBodyBuilder.add("\n// This just in: Field injections!\n")
                .add("// Buuut, if you are reading this, consider using something else. Constructor injections maybe?\n");

        for (InjectionContext.Field field : fields) {
            CodeBlock fetchVariable = fetchVariableForInjection(field.getType(), field.mayBeNull());
            if (field.requiresReflection()) {
                Name fieldName = field.getName();
                Logger.reflectionWarning(field.getVariableElement());
                ClassName invokeUpon = ClassName.get(ReflectionsHelper.class);
                methodBodyBuilder.addStatement("$T.setField($S, $L, $T.class, $L)", invokeUpon, fieldName, instanceName, wireInformation.primaryClassName(), fetchVariable);
            } else {
                methodBodyBuilder.addStatement("$L.$L = $L", instanceName, field.getName(), fetchVariable);
            }
        }
    }

    private void addPostConstructMethods(InjectionContext injectionContext, CodeBlock.Builder methodBodyBuilder, WireInformation wireInformation) {
        if (injectionContext.hasNoPostConstructMethod()) {
            return;
        }

        List<InjectionContext.PostConstructMethod> postConstructMethods = injectionContext.drainPostConstructMethod();

        methodBodyBuilder.add("\n// Now post construct invocations\n");
        postConstructMethods.forEach(postConstruct -> {
            if (postConstruct.requiresReflection()) {
                Logger.reflectionWarning(postConstruct.getExecutableElement());
                ClassName invoker = ClassName.get(ReflectionsHelper.class);
                ClassName invokeOn = wireInformation.primaryClassName();
                Name name = postConstruct.getMethodName();
                TypeName returnType = postConstruct.returnType();

                methodBodyBuilder.addStatement("$T.invokeMethod(instance, $T.class, $S, $T.class)", invoker, invokeOn, name, returnType);
            } else {
                methodBodyBuilder.addStatement("instance.$L()", postConstruct.getMethodName());
            }
        });
    }

    protected void initialize(InjectionContext injectionContext, WireInformation wireInformation, CodeBlock.Builder methodBodyBuilder) {

    }

    @Override
    public final void construct(WireInformation wireInformation, TypeSpec.Builder typeBuilder) {
        InjectionContext injectionContext = setupInjectionContext(wireInformation);
        TypeElement typeElement = wireInformation.getPrimaryWireType();
        MethodSpec.Builder method = privateMethod()
                .addParameter(WireRepository.class, "wiredTypes", Modifier.FINAL)
                .returns(TypeName.get(typeElement.asType()));
        CodeBlock.Builder methodBodyBuilder = CodeBlock.builder();

        initialize(injectionContext, wireInformation, methodBodyBuilder);
        if (injectionContext.hasNoFurtherInjectionQualities()) {
            methodBodyBuilder.addStatement("return new $T()", wireInformation.realClassName());
        } else {
            if (injectionContext.hasNoConstructorInjections()) {
                methodBodyBuilder.add("// No Parameters, just create a new instance \n")
                        .addStatement("$T $L = new $T()", wireInformation.primaryClassName(), instanceName, wireInformation.realClassName());
            } else {
                addConstructorInvocations(injectionContext, methodBodyBuilder, wireInformation);
            }

            if (injectionContext.hasFurtherInjectionQualities()) {
                addSetterInjections(injectionContext, methodBodyBuilder, wireInformation);
            }

            if (injectionContext.hasFurtherInjectionQualities()) {
                addFieldsInjections(injectionContext, methodBodyBuilder, wireInformation);
            }

            if (injectionContext.hasFurtherInjectionQualities()) {
                addPostConstructMethods(injectionContext, methodBodyBuilder, wireInformation);
            }

            if (!injectionContext.alreadyReturned()) {
                methodBodyBuilder.add("\n// And we are done! A fresh and newly created instance, ready for you! \n")
                        .addStatement("return $L", instanceName);
            }
        }

        appendPostReturn(methodBodyBuilder);
        method.addCode(methodBodyBuilder.build());
        typeBuilder.addMethod(method.build());
    }

    protected abstract void findFieldInjections(
            WireInformation wireInformation,
            InjectionContext injectionContext
    );

    protected abstract void findSetterInjections(
            WireInformation wireInformation,
            InjectionContext injectionContext
    );

    protected abstract void findConstructorInjections(
            WireInformation wireInformation,
            InjectionContext injectionContext
    );

    protected void appendPostReturn(CodeBlock.Builder builder) {
    }

    private InjectionContext setupInjectionContext(WireInformation wireInformation) {
        InjectionContext injectionContext = new InjectionContext();

        findConstructorInjections(wireInformation, injectionContext);
        findFieldInjections(wireInformation, injectionContext);
        findSetterInjections(wireInformation, injectionContext);

        wireInformation.getPrimaryWireType()
                .getEnclosedElements()
                .stream()
                .filter(it -> it.getAnnotation(PostConstruct.class) != null)
                .filter(it -> it instanceof ExecutableElement)
                .map(it -> (ExecutableElement) it)
                .forEach(injectionContext::announcePostConstructMethod);

        return injectionContext;
    }

    protected static class VariableName {

        private final String value;

        VariableName(String value) {
            this.value = Objects.requireNonNull(value, "The variable name may not be null");
        }
    }
}
