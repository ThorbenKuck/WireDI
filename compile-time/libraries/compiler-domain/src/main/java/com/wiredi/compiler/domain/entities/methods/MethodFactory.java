package com.wiredi.compiler.domain.entities.methods;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.ClassEntity;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface MethodFactory {

	void append(@NotNull MethodSpec.Builder builder, @NotNull ClassEntity<?> entity);

}
