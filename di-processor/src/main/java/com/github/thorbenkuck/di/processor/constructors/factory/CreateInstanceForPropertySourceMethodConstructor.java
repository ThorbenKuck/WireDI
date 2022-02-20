package com.github.thorbenkuck.di.processor.constructors.factory;

import com.github.thorbenkuck.di.annotations.properties.Named;
import com.github.thorbenkuck.di.annotations.properties.PropertySource;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.foundation.ProcessingException;
import com.github.thorbenkuck.di.processor.foundation.ProcessorContext;
import com.github.thorbenkuck.di.properties.TypedProperties;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
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

    public CreateInstanceForPropertySourceMethodConstructor() {
        this.types = ProcessorContext.getTypes();
        this.listTypeElement = ProcessorContext.getElements().getTypeElement(List.class.getName());
    }

    @Override
    protected List<String> findAndApplyConstructorParameters(
            CodeBlock.Builder builder,
            List<? extends VariableElement> parameters,
            WireInformation wireInformation
    ) {
        PropertySource annotation = wireInformation.findAnnotation(PropertySource.class)
                .orElseThrow(() ->  new ProcessingException(wireInformation.getPrimaryWireType(), "This class should have a @PropertySource annotation"));

        List<String> names = new ArrayList<>();
        int i = 0;

        hasOpenControlFlow = appendPropertiesConstruction(annotation, builder, wireInformation);
        for (VariableElement argument : parameters) {
            String variableName = "t" + i++;
            String key = getPropertyName(annotation, argument);

            if (types.isAssignable(types.asElement(argument.asType()).asType(), listTypeElement.asType())) {
                appendFetchingPropertyList(builder, variableName, key, ClassName.get(argument.asType()));
            } else {
                appendFetchingProperty(builder, variableName, key, ClassName.get(argument.asType()));
            }

            names.add(variableName);
        }

        return names;
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

    private boolean appendPropertiesConstruction(
            PropertySource propertyAnnotation,
            CodeBlock.Builder builder,
            WireInformation wireInformation
    ) {
        String file = propertyAnnotation.file();
        ClassName typedPropertiesClassName = ClassName.get(TypedProperties.class);
        if (file.isEmpty()) {
            builder.addStatement("$T properties = wiredTypes.properties()", typedPropertiesClassName);
            return false;
        } else {
            PropertySource.Lifecycle lifecycle = propertyAnnotation.lifecycle();
            if (lifecycle == PropertySource.Lifecycle.RUNTIME) {
                builder.beginControlFlow("try ($T properties = $T.fromClassPath($S))", typedPropertiesClassName, typedPropertiesClassName, file);
                return true;
            } else {
                try {
                    FileObject resource = ProcessorContext.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", file);
                    String content = dump(resource);
                    builder.addStatement("$T rawPropertyContent = $S", String.class, content);
                    builder.beginControlFlow("try ($T properties = $T.fromString($L))", typedPropertiesClassName, typedPropertiesClassName, "rawPropertyContent");
                    return true;
                } catch (IOException e) {
                    throw new ProcessingException(wireInformation.getPrimaryWireType(), "Could not find the properties: \"" + file + "\" in the classpath at compile time");
                }
            }
        }
    }

    private String dump(
            FileObject fileObject
    ) throws IOException {
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

    private void appendFetchingProperty(
            CodeBlock.Builder builder,
            String variableName,
            String propertyKey,
            TypeName propertyType
    ) {
        builder.addStatement("$T $L = properties.getTyped($S, $T.class)", propertyType, variableName, propertyKey, propertyType);
    }

    private void appendFetchingPropertyList(
            CodeBlock.Builder builder,
            String variableName,
            String propertyKey,
            TypeName propertyType
    ) {
        builder.addStatement("List<$T> $L = properties.getAll($S, $T.class)", propertyType, variableName, propertyKey, propertyType);
    }

    @Override
    protected void appendPostReturn(CodeBlock.Builder builder) {
        if(hasOpenControlFlow) {
            builder.endControlFlow();
        }
    }

    @Override
    protected void findFieldInjections(TypeElement typeElement, InjectionContext injectionContext) {
    }
}
