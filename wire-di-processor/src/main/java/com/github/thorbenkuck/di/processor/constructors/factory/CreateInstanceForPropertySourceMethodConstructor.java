package com.github.thorbenkuck.di.processor.constructors.factory;

import com.github.thorbenkuck.di.annotations.properties.Properties;
import com.github.thorbenkuck.di.annotations.properties.PropertyName;
import com.github.thorbenkuck.di.processor.FetchAnnotated;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.Logger;
import com.github.thorbenkuck.di.processor.exceptions.ProcessingException;
import com.github.thorbenkuck.di.processor.ProcessorContext;
import com.github.thorbenkuck.di.properties.Keys;
import com.github.thorbenkuck.di.properties.TypedProperties;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.inject.Inject;
import javax.inject.Named;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.util.List;

public class CreateInstanceForPropertySourceMethodConstructor extends CreateInstanceMethodConstructor {

    private final Types types;
    private final TypeElement listTypeElement;
    private boolean hasOpenControlFlow = false;
    private Properties annotation;

    public CreateInstanceForPropertySourceMethodConstructor() {
        this.types = ProcessorContext.getTypes();
        this.listTypeElement = ProcessorContext.mapWithElements(elements -> elements.getTypeElement(List.class.getName()));
    }

    @Override
    protected VariableName fetchConstructorVariable(InjectionContext.ConstructorParameter constructorParameter, CodeBlock.Builder builder) {
        String variableName = "property" + constructorParameter.getIndex();
        String propertyKey = getPropertyName(annotation, constructorParameter.getVariableElement());
        TypeMirror variableTypeMirror = constructorParameter.getVariableElement().asType();

        if (!variableTypeMirror.getKind().isPrimitive() && types.isAssignable(types.asElement(variableTypeMirror).asType(), listTypeElement.asType())) {
            propertyListToVariable(builder, variableName, propertyKey, ClassName.get(variableTypeMirror));
        } else {
            singlePropertyToVariable(builder, variableName, propertyKey, ClassName.get(variableTypeMirror));
        }
        return new VariableName(variableName);
    }

    @Override
    protected void initialize(InjectionContext injectionContext, WireInformation wireInformation, CodeBlock.Builder methodBodyBuilder) {
        annotation = wireInformation.findAnnotation(Properties.class)
                .orElseThrow(() ->  new ProcessingException(wireInformation.getPrimaryWireType(), "This class should have a @PropertySource annotation"));

        String file = annotation.file();
        ClassName typedPropertiesClassName = ClassName.get(TypedProperties.class);
        if (file.isEmpty()) {
            methodBodyBuilder.addStatement("$T properties = wiredTypes.properties()", typedPropertiesClassName);
        } else {
            Properties.Lifecycle lifecycle = annotation.lifecycle();
            if (lifecycle == Properties.Lifecycle.RUNTIME) {
                methodBodyBuilder.beginControlFlow("try ($T properties = $T.fromClassPath($S))", typedPropertiesClassName, typedPropertiesClassName, file);
                hasOpenControlFlow = true;
            } else {
                try {
                    FileObject resource = ProcessorContext.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", file);
                    String content = dumpFileToStringContent(resource);
                    methodBodyBuilder.addStatement("$T rawPropertyContent = $S", String.class, content);
                    methodBodyBuilder.beginControlFlow("try ($T properties = $T.fromString($L))", typedPropertiesClassName, typedPropertiesClassName, "rawPropertyContent");
                    hasOpenControlFlow = true;
                } catch (IOException e) {
                    throw new ProcessingException(wireInformation.getPrimaryWireType(), "Could not find the properties: \"" + file + "\" in the classpath at compile time");
                }
            }
        }
    }

    @Override
    protected CodeBlock fetchVariableForInjection(TypeElement typeElement, boolean nullable) {
        return null;
    }

    @Override
    protected void findFieldInjections(WireInformation wireInformation, InjectionContext injectionContext) {
        List<VariableElement> annotatedFields = FetchAnnotated.fields(wireInformation.getPrimaryWireType(), Inject.class);
        annotatedFields.forEach(injectionContext::announceFieldInjection);
    }

    @Override
    protected void findSetterInjections(WireInformation wireInformation, InjectionContext injectionContext) {
        List<ExecutableElement> methods = FetchAnnotated.methods(wireInformation.getPrimaryWireType(), Inject.class);

        for (ExecutableElement method : methods) {
            if(method.getParameters().size() != 1) {
                Logger.error("Injections in only enabled for methods with one parameters, this method contains %s", Integer.toString(method.getParameters().size()));
            } else {
                injectionContext.announceSetterInjection(method);
            }
        }
    }

    @Override
    protected void findConstructorInjections(WireInformation wireInformation, InjectionContext injectionContext) {
        wireInformation.getPrimaryConstructor().ifPresent(constructor -> {
            for (VariableElement parameter : constructor.getParameters()) {
                injectionContext.announceConstructorParameter(parameter);
            }
        });
    }

    @Override
    protected void appendPostReturn(CodeBlock.Builder builder) {
        if(hasOpenControlFlow) {
            builder.endControlFlow();
        }
    }

    private String getPropertyName(Properties propertiesAnnotation, VariableElement element) {
        String prefix = propertiesAnnotation.prefix();
        if(prefix.endsWith(".")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        String suffix;


        if(element.getAnnotation(PropertyName.class) != null) {
            PropertyName annotation = element.getAnnotation(PropertyName.class);
            String message = annotation.value();
            if(annotation.format()) {
                message = Keys.format(message);
            }
            suffix = message;
        } else if(element.getAnnotation(Named.class) != null) {
            Named annotation = element.getAnnotation(Named.class);
            if(annotation.value().isEmpty()) {
                suffix = Keys.format(element.getSimpleName().toString());
            } else {
                suffix = Keys.format(annotation.value());
            }
        } else {
            suffix = Keys.format(element.getSimpleName().toString());
        }

        if(prefix.isEmpty()) {
            return suffix;
        }

        if(!suffix.startsWith(".")) {
            suffix = "." + suffix;
        }
        return prefix + suffix;
    }

    private String dumpFileToStringContent(FileObject fileObject) throws IOException {
        try (InputStream properties = fileObject.openInputStream();
             StringWriter stringWriter = new StringWriter();
             StringWriter resultWriter = new StringWriter()) {

            java.util.Properties compileTimeProperties = new java.util.Properties();
            compileTimeProperties.load(properties);
            compileTimeProperties.store(stringWriter, "");

            try (BufferedReader stringReader = new BufferedReader(new StringReader(stringWriter.toString()))) {

                String line;
                while ((line = stringReader.readLine()) != null) {
                    if (!line.trim().startsWith("#")) {
                        resultWriter.append(line).append(System.lineSeparator());
                    }
                }

                return resultWriter.toString();
            }
        }
    }

    private void singlePropertyToVariable(
            CodeBlock.Builder builder,
            String variableName,
            String propertyKey,
            TypeName propertyType
    ) {
        builder.addStatement("$T $L = properties.getTyped($S, $T.class)", propertyType, variableName, propertyKey, propertyType);
    }

    private void propertyListToVariable(
            CodeBlock.Builder builder,
            String variableName,
            String propertyKey,
            TypeName propertyType
    ) {
        builder.addStatement("List<$T> $L = properties.getAll($S, $T.class)", propertyType, variableName, propertyKey, propertyType);
    }
}
