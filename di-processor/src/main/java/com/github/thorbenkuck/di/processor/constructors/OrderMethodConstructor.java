package com.github.thorbenkuck.di.processor.constructors;

import com.github.thorbenkuck.di.annotations.WirePriority;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class OrderMethodConstructor implements MethodConstructor {

    @Override
    public void construct(TypeElement typeElement, TypeSpec.Builder builder) {
        WirePriority annotation = typeElement.getAnnotation(WirePriority.class);
        if(annotation != null) {
            MethodSpec methodSpec = MethodSpec.methodBuilder("priority")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .returns(int.class)
                    .addCode(CodeBlock.builder().addStatement("return $L", annotation.value()).build())
                    .build();

            builder.addMethod(methodSpec);
        }
    }

}
