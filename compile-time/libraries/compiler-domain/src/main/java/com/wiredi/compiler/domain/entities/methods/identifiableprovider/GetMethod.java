package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class GetMethod implements StandaloneMethodFactory {

    private final TypeMirror returnType;

    public GetMethod(TypeMirror returnType) {
        this.returnType = returnType;
    }

    @Override
    public void append(MethodSpec.@NotNull Builder builder, @NotNull ClassEntity<?> entity) {
        List<Modifier> getMethodModifier = new ArrayList<>(List.of(Modifier.PUBLIC, Modifier.FINAL));

        CodeBlock.Builder getCodeBlock = CodeBlock.builder();
        getCodeBlock.addStatement("return createInstance(wireContainer, concreteType)");


        builder.addModifiers(getMethodModifier)
                .addAnnotation(Override.class)
                .addParameter(
                        ParameterSpec.builder(WireContainer.class, "wireContainer", Modifier.FINAL)
                                .addAnnotation(NotNull.class)
                                .build()
                )
                .addParameter(
                        ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(TypeIdentifier.class), ClassName.get(returnType)), "concreteType", Modifier.FINAL)
                                .addAnnotation(NotNull.class)
                                .build()
                )
                .addCode(getCodeBlock.build())
                .returns(TypeName.get(returnType))
                .build();
    }

    @Override
    public @NotNull String methodName() {
        return "get";
    }
}
