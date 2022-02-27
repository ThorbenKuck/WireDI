package com.github.thorbenkuck.di.processor.constructors.factory;

import com.github.thorbenkuck.di.annotations.properties.Named;
import com.github.thorbenkuck.di.annotations.properties.PropertySource;
import com.github.thorbenkuck.di.processor.FetchAnnotated;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.foundation.Logger;
import com.github.thorbenkuck.di.processor.foundation.ProcessingException;
import com.github.thorbenkuck.di.processor.foundation.ProcessorContext;
import com.github.thorbenkuck.di.properties.TypedProperties;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CreateInstanceForPropertySourceMethodConstructor extends CreateInstanceMethodConstructor {

    private final Types types;
    private final TypeElement listTypeElement;
    private boolean hasOpenControlFlow = false;
    private PropertySource annotation;

    public CreateInstanceForPropertySourceMethodConstructor() {
        this.types = ProcessorContext.getTypes();
        this.listTypeElement = ProcessorContext.getElements().getTypeElement(List.class.getName());
    }

    @Override
    protected VariableName fetchConstructorVariable(InjectionContext.ConstructorParameter constructorParameter, CodeBlock.Builder builder) {
        String variableName = "property" + constructorParameter.getIndex();
        String propertyKey = getPropertyName(annotation, constructorParameter.getVariableElement());
        TypeMirror variableTypeMirror = constructorParameter.getVariableElement().asType();

        if (types.isAssignable(types.asElement(variableTypeMirror).asType(), listTypeElement.asType())) {
            propertyListToVariable(builder, variableName, propertyKey, ClassName.get(variableTypeMirror));
        } else {
            singlePropertyToVariable(builder, variableName, propertyKey, ClassName.get(variableTypeMirror));
        }
        return new VariableName(variableName);
    }

    @Override
    protected void initialize(InjectionContext injectionContext, WireInformation wireInformation, CodeBlock.Builder methodBodyBuilder) {
        annotation = wireInformation.findAnnotation(PropertySource.class)
                .orElseThrow(() ->  new ProcessingException(wireInformation.getPrimaryWireType(), "This class should have a @PropertySource annotation"));

        String file = annotation.file();
        ClassName typedPropertiesClassName = ClassName.get(TypedProperties.class);
        if (file.isEmpty()) {
            methodBodyBuilder.addStatement("$T properties = wiredTypes.properties()", typedPropertiesClassName);
        } else {
            PropertySource.Lifecycle lifecycle = annotation.lifecycle();
            if (lifecycle == PropertySource.Lifecycle.RUNTIME) {
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
                Logger.error("Injections in only enabled for methods with one parameters, this method contains " + method.getParameters().size());
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

    private String getPropertyName(PropertySource propertySourceAnnotation, VariableElement element) {
        String prefix = propertySourceAnnotation.prefix();
        if(prefix.endsWith(".")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        String suffix;


        if(element.getAnnotation(Named.class) != null) {
            suffix = element.getAnnotation(Named.class).value();
        } else {
            suffix = element.getSimpleName().toString();
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

            Properties compileTimeProperties = new Properties();
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
