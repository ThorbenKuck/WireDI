package com.wiredi.compiler.domain.entities.methods;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.ClassEntity;

@FunctionalInterface
public interface MethodFactory {

	void append(MethodSpec.Builder builder, ClassEntity<?> entity);

}
