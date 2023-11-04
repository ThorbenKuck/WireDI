package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;

import javax.lang.model.element.Modifier;

public class OrderMethod implements StandaloneMethodFactory {

    private final int order;

    public OrderMethod(int order) {
        this.order = order;
    }

    @Override
    public void append(MethodSpec.Builder builder, ClassEntity<?> entity) {
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(TypeName.INT)
                .addAnnotation(Override.class)
                .addStatement("return $L", order)
                .build();
    }

    @Override
    public String methodName() {
        return "getOrder";
    }
}
