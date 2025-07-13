package com.wiredi.compiler.domain.entities.methods.aspecthandler;

import com.squareup.javapoet.*;
import com.wiredi.annotations.aspects.Aspect;
import com.wiredi.runtime.aspects.RootMethod;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;
import com.wiredi.compiler.domain.values.AspectHandlerMethod;
import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class AppliesToMethod implements StandaloneMethodFactory {

    private static final String ANNOTATION_PARAMETER_NAME = "instance";
    private static final String ROOT_METHOD_PARAMETER_NAME = "rootMethod";
    private final AspectHandlerMethod factoryMethod;

    public AppliesToMethod(AspectHandlerMethod factoryMethod) {
        this.factoryMethod = factoryMethod;
    }

    @Override
    public void append(MethodSpec.@NotNull Builder builder, @NotNull ClassEntity<?> entity) {
        TypeMirror aspectHandler = Annotations.extractType(factoryMethod.annotation(), Aspect::around);
        CodeBlock methodBody = CodeBlock.builder()
                .add("return $L.className().equals($S)", ANNOTATION_PARAMETER_NAME, aspectHandler.toString())
                .build();

        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(ParameterSpec.builder(ClassName.get(AnnotationMetadata.class), ANNOTATION_PARAMETER_NAME)
                        .addModifiers(Modifier.FINAL)
                        .addAnnotation(NotNull.class)
                        .build())
                .addParameter(ParameterSpec.builder(ClassName.get(RootMethod.class), ROOT_METHOD_PARAMETER_NAME)
                        .addModifiers(Modifier.FINAL)
                        .addAnnotation(NotNull.class)
                        .build())
                .returns(TypeName.BOOLEAN)
                .addStatement(methodBody);
    }

    @Override
    public @NotNull String methodName() {
        return "appliesTo";
    }
}
