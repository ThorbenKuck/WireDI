package com.wiredi.compiler.domain.entities.methods.aspecthandler;

import com.squareup.javapoet.*;
import com.wiredi.aspects.links.RootMethod;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;
import com.wiredi.compiler.domain.values.AspectHandlerMethod;
import com.wiredi.domain.AnnotationMetaData;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;

public class AppliesToMethod implements StandaloneMethodFactory {

    private static final String ANNOTATION_PARAMETER_NAME = "annotation";
    private static final String ROOT_METHOD_PARAMETER_NAME = "rootMethod";
    private final AspectHandlerMethod factoryMethod;

    public AppliesToMethod(AspectHandlerMethod factoryMethod) {
        this.factoryMethod = factoryMethod;
    }

    @Override
    public void append(MethodSpec.Builder builder, ClassEntity<?> entity) {
        CodeBlock methodBody = CodeBlock.builder()
                .add("return $L.className().equals($S)", ANNOTATION_PARAMETER_NAME, factoryMethod.enclosingType().toString())
                .build();

        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(ParameterSpec.builder(ClassName.get(AnnotationMetaData.class), ANNOTATION_PARAMETER_NAME)
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
    public String methodName() {
        return "appliesTo";
    }
}
