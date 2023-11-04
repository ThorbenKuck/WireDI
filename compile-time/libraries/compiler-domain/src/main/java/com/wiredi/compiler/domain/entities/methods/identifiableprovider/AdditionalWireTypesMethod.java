package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.TypeIdentifiers;
import com.wiredi.compiler.domain.entities.methods.MethodFactory;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;
import com.wiredi.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdditionalWireTypesMethod implements StandaloneMethodFactory {

    private static final TypeName TYPE_IDENTIFIER_LIST = ParameterizedTypeName.get(
            ClassName.get(List.class),
            ParameterizedTypeName.get(
                    ClassName.get(TypeIdentifier.class),
                    WildcardTypeName.subtypeOf(Object.class)
            )
    );
    private final List<TypeMirror> typeElements;
    private final TypeIdentifiers typeIdentifiers;

    public AdditionalWireTypesMethod(List<TypeMirror> typeElements, TypeIdentifiers typeIdentifiers) {
        this.typeElements = typeElements;
        this.typeIdentifiers = typeIdentifiers;
    }

    @Override
    public void append(MethodSpec.Builder builder, ClassEntity<?> entity) {
        List<CodeBlock> typeIdentifier = typeIdentifiers();

        entity.addField(TYPE_IDENTIFIER_LIST, "ADDITIONAL_WIRE_TYPES", field ->
                field.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer(
                                CodeBlock.builder()
                                        .add("$T.of(\n", List.class)
                                        .indent()
                                        .add(CodeBlock.join(typeIdentifier, ",\n"))
                                        .unindent()
                                        .add("\n)")
                                        .build()
                        )
                        .build());

        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(TYPE_IDENTIFIER_LIST)
                .addAnnotation(Override.class)
                .addAnnotation(NotNull.class)
                .addStatement("return ADDITIONAL_WIRE_TYPES")
                .build();
    }

    private List<CodeBlock> typeIdentifiers() {
        return typeElements.stream()
                .filter(Objects::nonNull)
                .filter(it -> it.getKind() != TypeKind.NONE)
                .filter(it -> !it.equals(typeIdentifiers.objectType()))
                .map(typeIdentifiers::newTypeIdentifier)
                .toList();
    }

    @Override
    public String methodName() {
        return "additionalWireTypes";
    }

    @Override
    public boolean applies(ClassEntity<?> entity) {
        return !typeIdentifiers().isEmpty();
    }
}
