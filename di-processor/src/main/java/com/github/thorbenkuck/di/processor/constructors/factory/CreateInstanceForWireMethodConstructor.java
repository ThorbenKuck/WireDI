package com.github.thorbenkuck.di.processor.constructors.factory;

import com.github.thorbenkuck.di.annotations.Nullable;
import com.github.thorbenkuck.di.annotations.properties.Property;
import com.github.thorbenkuck.di.processor.FetchAnnotated;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.foundation.ProcessorContext;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

public class CreateInstanceForWireMethodConstructor extends CreateInstanceMethodConstructor {

    private final Types types;
    private final TypeElement listTypeElement;

    public CreateInstanceForWireMethodConstructor() {
        this.types = ProcessorContext.getTypes();
        this.listTypeElement = ProcessorContext.getElements().getTypeElement(List.class.getName());
    }

    @Override
    protected List<String> findAndApplyConstructorParameters(
            CodeBlock.Builder builder,
            List<? extends VariableElement> parameters,
            WireInformation wireInformation
    ) {
        List<String> names = new ArrayList<>();
        int i = 0;

        for (VariableElement argument : parameters) {
            String variableName = "t" + i++;
            Property propertyAnnotation = argument.getAnnotation(Property.class);
            if (propertyAnnotation != null) {
                appendFetchingProperty(propertyAnnotation, builder, argument, variableName);
            } else {
                if (types.isAssignable(types.asElement(argument.asType()).asType(), listTypeElement.asType())) {
                    appendFetchingList(builder, argument, variableName);
                } else {
                    appendFetchingSingleVariable(builder, argument, variableName);
                }
            }

            names.add(variableName);
        }

        return names;
    }

    private void appendFetchingProperty(
            Property propertyAnnotation,
            CodeBlock.Builder builder,
            VariableElement argument,
            String variableName
    ) {
        String name = propertyAnnotation.value();
        String defaultValue = propertyAnnotation.defaultValue();
        if (defaultValue.isEmpty()) {
            builder.addStatement("$T $L = wiredTypes.properties().get($S)", ClassName.get(argument.asType()), variableName, name);
        } else {
            builder.addStatement("$T $L = wiredTypes.properties().get($S, $S)", ClassName.get(argument.asType()), variableName, name, defaultValue);
        }
    }

    private void appendFetchingList(
            CodeBlock.Builder builder,
            VariableElement argument,
            String variableName
    ) {
        DeclaredType declaredType = (DeclaredType) argument.asType();
        TypeName listTypeName = ClassName.get(declaredType.getTypeArguments().get(0));
        builder.addStatement("$T<$T> $L = wiredTypes.getAll($T.class)", ClassName.get(List.class), listTypeName, variableName, listTypeName);
    }

    private void appendFetchingSingleVariable(
            CodeBlock.Builder builder,
            VariableElement argument,
            String variableName
    ) {
        TypeName typeName = ClassName.get(argument.asType());
        if (argument.getAnnotation(Nullable.class) == null) {
            builder.addStatement("$T $L = wiredTypes.requireInstance($T.class)", typeName, variableName, typeName);
        } else {
            builder.addStatement("$T $L = wiredTypes.getInstance($T.class)", typeName, variableName, typeName);
        }
    }

    @Override
    protected void findFieldInjections(TypeElement typeElement, InjectionContext injectionContext) {
        List<VariableElement> annotatedFields = FetchAnnotated.fields(typeElement, Inject.class);
        annotatedFields.forEach(injectionContext::announceFieldInjection);
    }
}
