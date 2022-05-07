package com.github.thorbenkuck.di.processor.builder.constructors.factory;

import com.github.thorbenkuck.di.annotations.properties.Property;
import com.github.thorbenkuck.di.processor.FetchAnnotated;
import com.github.thorbenkuck.di.processor.ProcessorContext;
import com.github.thorbenkuck.di.processor.WireInformation;
import com.github.thorbenkuck.di.processor.Logger;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.inject.Inject;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Types;
import java.util.List;

public class CreateInstanceForWireMethodConstructor extends CreateInstanceMethodConstructor {

    private final Types types;
    private final TypeElement listTypeElement;

    public CreateInstanceForWireMethodConstructor() {
        this.types = ProcessorContext.getTypes();
        this.listTypeElement = ProcessorContext.mapWithElements(elements -> elements.getTypeElement(List.class.getName()));
    }

    @Override
    protected VariableName fetchConstructorVariable(
            InjectionContext.ConstructorParameter constructorParameter,
            CodeBlock.Builder builder
    ) {
        ClassName className = ClassName.get(constructorParameter.getType());
        String name = "constructorDependency" + constructorParameter.getIndex();

        if (constructorParameter.getVariableElement().getAnnotation(Property.class) != null) {
            fetchPropertyConstructorVariable(constructorParameter, builder, name, className);
        } else {
            fetchDependencyConstructorVariable(constructorParameter, builder, name, className);
        }

        return new VariableName(name);
    }

    private void fetchPropertyConstructorVariable(
            InjectionContext.ConstructorParameter constructorParameter,
            CodeBlock.Builder builder,
            String name,
            ClassName className
    ) {
        Property property = constructorParameter.getVariableElement().getAnnotation(Property.class);
        if (types.isAssignable(types.asElement(constructorParameter.getType().asType()).asType(), listTypeElement.asType())) {
            Logger.error(constructorParameter.getVariableElement(), "Listed property fetching is currently not implemented");
        } else {
            builder.addStatement("final $T $L = $L", className, name, getProperty(property.value(), className, property.defaultValue()));
        }
    }

    private void fetchDependencyConstructorVariable(
            InjectionContext.ConstructorParameter constructorParameter,
            CodeBlock.Builder builder,
            String name,
            ClassName className
    ) {
        if (types.isAssignable(types.asElement(constructorParameter.getType().asType()).asType(), listTypeElement.asType())) {
            appendFetchingList(builder, constructorParameter.getVariableElement(), name);
        } else {
            appendFetchingSingleVariable(constructorParameter, builder, className, name);
        }
    }

    @Override
    protected CodeBlock fetchVariableForInjection(TypeElement typeElement, boolean nullable) {
        ClassName className = ClassName.get(typeElement);

        if (types.isAssignable(types.asElement(typeElement.asType()).asType(), listTypeElement.asType())) {
            return getAll(className);
        } else {
            if (nullable) {
                return tryGet(className);
            } else {
                return get(className);
            }
        }
    }

    @Override
    protected void findFieldInjections(WireInformation wireInformation, InjectionContext injectionContext) {
        List<VariableElement> annotatedFields = FetchAnnotated.fields(wireInformation.getPrimaryWireType(), Inject.class);
        annotatedFields.forEach(injectionContext::announceFieldInjection);
    }

    @Override
    protected void findSetterInjections(WireInformation wireInformation, InjectionContext injectionContext) {
        wireInformation.getPrimaryWireType()
                .getEnclosedElements()
                .stream()
                .filter(element -> element.getKind() == ElementKind.METHOD)
                .map(element -> (ExecutableElement) element)
                .filter(it -> it.getAnnotation(Inject.class) != null)
                .forEach(injectionContext::announceSetterInjection);
    }

    @Override
    protected void findConstructorInjections(WireInformation wireInformation, InjectionContext injectionContext) {
        wireInformation.getPrimaryConstructor().ifPresent(constructor -> {
            for (VariableElement parameter : constructor.getParameters()) {
                injectionContext.announceConstructorParameter(parameter);
            }
        });
    }

    private void appendFetchingSingleVariable(
            InjectionContext.ConstructorParameter constructorParameter,
            CodeBlock.Builder builder,
            ClassName variableType,
            String name
    ) {
        if (constructorParameter.mayBeNull()) {
            builder.addStatement("final $T $L = $L", variableType, name, tryGet(variableType));
        } else {
            builder.addStatement("final $T $L = $L", variableType, name, get(variableType));
        }
    }

    private void appendFetchingList(
            CodeBlock.Builder builder,
            VariableElement argument,
            String variableName
    ) {
        DeclaredType declaredType = (DeclaredType) argument.asType();
        TypeName listTypeName = ClassName.get(declaredType.getTypeArguments().get(0));
        builder.addStatement("final $T<$T> $L = $L", ClassName.get(List.class), listTypeName, variableName, getAll(listTypeName));
    }

    private CodeBlock tryGet(ClassName variableType) {
        return CodeBlock.builder()
                .add("wireRepository.tryGet($T.class).orElse(null)", variableType)
                .build();
    }

    private CodeBlock get(ClassName variableType) {
        return CodeBlock.builder()
                .add("wireRepository.get($T.class)", variableType)
                .build();
    }

    private CodeBlock getAll(TypeName variableType) {
        return CodeBlock.builder()
                .add("wireRepository.getAll($T.class)", variableType)
                .build();
    }

    private CodeBlock getProperty(String propertyName, TypeName variableType, String defaultValue) {
        return CodeBlock.builder()
                .add("wireRepository.properties().getTyped($S, $T.class, $S)", propertyName, variableType, defaultValue)
                .build();
    }

    private CodeBlock getPropertyList(String propertyName, TypeName variableType, String defaultValue) {
        return CodeBlock.builder()
                .add("wireRepository.properties().getTyped($S, $T.class, $S)", propertyName, variableType, defaultValue)
                .build();
    }
}
