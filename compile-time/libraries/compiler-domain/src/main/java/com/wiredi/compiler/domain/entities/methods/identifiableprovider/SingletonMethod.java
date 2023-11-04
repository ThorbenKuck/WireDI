package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.methods.MethodFactory;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;

import javax.lang.model.element.Modifier;

public class SingletonMethod implements StandaloneMethodFactory {

    private final boolean singleton;

    public SingletonMethod(boolean singleton) {
        this.singleton = singleton;
    }

    @Override
    public void append(MethodSpec.Builder builder, ClassEntity<?> entity) {
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .addStatement("return $L", singleton)
                .returns(TypeName.BOOLEAN)
                .build();
    }

    @Override
    public String methodName() {
        return "isSingleton";
    }

    @Override
    public boolean applies(ClassEntity<?> entity) {
        return !singleton;
    }
}
