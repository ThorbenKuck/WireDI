package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.ClassEntity;
import com.wiredi.compiler.domain.entities.methods.MethodFactory;
import com.wiredi.compiler.domain.entities.methods.StandaloneMethodFactory;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;

public class PrimaryMethod implements StandaloneMethodFactory {

    private final boolean isPrimary;

    public PrimaryMethod(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    @Override
    public void append(MethodSpec.@NotNull Builder builder, @NotNull ClassEntity<?> entity) {
        builder.returns(ClassName.BOOLEAN)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return true")
                .addAnnotation(Override.class);
    }

    @Override
    public @NotNull String methodName() {
        return "primary";
    }

    @Override
    public boolean applies(@NotNull ClassEntity<?> entity) {
        return isPrimary;
    }
}
