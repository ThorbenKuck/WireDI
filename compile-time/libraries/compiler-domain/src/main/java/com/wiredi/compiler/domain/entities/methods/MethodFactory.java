package com.wiredi.compiler.domain.entities.methods;

import com.squareup.javapoet.TypeSpec;
import com.wiredi.compiler.domain.AbstractClassEntity;

public interface MethodFactory {

	void append(TypeSpec.Builder builder, AbstractClassEntity<?> entity);

}
