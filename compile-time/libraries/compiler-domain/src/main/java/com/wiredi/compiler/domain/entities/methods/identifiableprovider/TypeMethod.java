package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.TypeIdentifiers;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class TypeMethod implements StandaloneMethodFactory {

    private final TypeIdentifiers typeIdentifiers;
    private final TypeMirror primaryWireType;

    public TypeMethod(TypeIdentifiers typeIdentifiers, TypeMirror primaryWireType) {
        this.typeIdentifiers = typeIdentifiers;
        this.primaryWireType = primaryWireType;
    }

    @Override
    public void append(MethodSpec.@NotNull Builder builder, @NotNull ClassEntity<?> entity) {
        String variableName = "PRIMARY_WIRE_TYPE";
        ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get(TypeIdentifier.class), TypeName.get(primaryWireType));

        entity.addField(typeName, variableName, field -> field.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(typeIdentifiers.newTypeIdentifier(primaryWireType))
        );

        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .addAnnotation(NotNull.class)
                .returns(typeName)
                .addStatement("return $L", variableName);
    }

    @Override
    public @NotNull String methodName() {
        return "type";
    }
}
