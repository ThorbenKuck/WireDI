package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.entities.methods.MethodFactory;

import javax.lang.model.element.Modifier;

public class SingletonMethod implements MethodFactory {

	private final boolean singleton;

	public SingletonMethod(boolean singleton) {
		this.singleton = singleton;
	}

	@Override
	public void append(TypeSpec.Builder builder, AbstractClassEntity<?> entity) {
		if (singleton) {
			return;
		}

		builder.addMethod(
				MethodSpec.methodBuilder("isSingleton")
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.addAnnotation(Override.class)
						.addStatement("return $L", singleton)
						.returns(TypeName.BOOLEAN)
						.build()
		);
	}
}
