package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.*;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class GetMethod implements StandaloneMethodFactory {

    private final boolean singleton;
    private final TypeMirror returnType;

    public GetMethod(boolean singleton, TypeMirror returnType) {
        this.singleton = singleton;
        this.returnType = returnType;
    }

    @Override
    public void append(MethodSpec.Builder builder, ClassEntity<?> entity) {
        List<Modifier> getMethodModifier = new ArrayList<>(List.of(Modifier.PUBLIC, Modifier.FINAL));

        CodeBlock.Builder getCodeBlock = CodeBlock.builder();
        if (singleton) {
            getCodeBlock.addStatement("return instance.getOrSet(() -> createInstance(wireRepository, concreteType))");

            entity.addField(ParameterizedTypeName.get(ClassName.get(Value.class), TypeName.get(entity.rootType())), "instance", (field) ->
                    field.addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                            .initializer("$T.empty()", Value.class)
                            .build()
            );
            getMethodModifier.add(Modifier.SYNCHRONIZED);
        } else {
            getCodeBlock.addStatement("return createInstance(wireRepository, concreteType)");
        }


        builder.addModifiers(getMethodModifier)
                .addAnnotation(Override.class)
                .addParameter(
                        ParameterSpec.builder(WireRepository.class, "wireRepository", Modifier.FINAL)
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
    public String methodName() {
        return "get";
    }
}
