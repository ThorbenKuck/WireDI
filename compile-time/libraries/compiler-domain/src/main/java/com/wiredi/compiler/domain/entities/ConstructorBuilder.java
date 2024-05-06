package com.wiredi.compiler.domain.entities;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ConstructorBuilder {

    public final Map<TypeName, String> parameters = new HashMap<>();
    public final List<String> superInvocations = new ArrayList<>();
    public final List<Field> fieldsToInitialize = new ArrayList<>();

    public void addParameters(List<? extends VariableElement> typeParameters) {
        typeParameters.forEach(variableElement -> addParameter(TypeName.get(variableElement.asType()), variableElement.getSimpleName().toString()));
    }

    public void addParameters(List<? extends VariableElement> typeParameters, boolean relevantForSuperInvocation) {
        typeParameters.forEach(variableElement -> addParameter(TypeName.get(variableElement.asType()), variableElement.getSimpleName().toString(), relevantForSuperInvocation));
    }

    public String addParameter(TypeName typeName, String parameterName) {
        return parameters.computeIfAbsent(typeName, it -> parameterName);
    }

    public String addParameter(TypeName typeName, String parameterName, boolean relevantForSuperInvocation) {
        String parameter = parameters.computeIfAbsent(typeName, it -> parameterName);
        if (relevantForSuperInvocation) {
            superInvocations.add(parameter);
        }

        return parameter;
    }

    public void initializeField(TypeName typeName, String name) {
        initializeField(typeName, name, parameterName -> CodeBlock.of("this.$L = $L", name, parameterName));
    }

    public void initializeField(TypeName typeName, String name, Function<String, CodeBlock> initializer) {
        String parameterName = addParameter(typeName, name);
        this.fieldsToInitialize.add(new Field(initializer.apply(parameterName)));
    }

    public MethodSpec.Builder initializeConstructor() {
        return initializeConstructor(null);
    }

    public MethodSpec.Builder initializeConstructor(@Nullable CodeBlock initializer) {
        MethodSpec.Builder constructorMethod = MethodSpec.constructorBuilder();
        parameters.forEach((typeName, parameterName) -> constructorMethod.addParameter(
                        ParameterSpec.builder(typeName, parameterName)
                                .addModifiers(Modifier.FINAL)
                                .build()
                )
        );

        if (!superInvocations.isEmpty()) {
            constructorMethod.addStatement("super($L)", String.join(", ", superInvocations));
        }
        fieldsToInitialize.forEach(field -> constructorMethod.addStatement(field.initializer));
        if (initializer != null) {
            constructorMethod.addCode(initializer);
        }

        return constructorMethod;
    }

    public record Field(CodeBlock initializer) {
    }
}
