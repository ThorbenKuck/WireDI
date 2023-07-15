package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.entities.methods.MethodFactory;

import javax.lang.model.element.Modifier;

public class PrimaryMethod implements MethodFactory {

	private final boolean isPrimary;

	public PrimaryMethod(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	@Override
	public void append(TypeSpec.Builder builder, AbstractClassEntity<?> entity) {
		if(!isPrimary) {
			return;
		}

		builder.addMethod(
				MethodSpec.methodBuilder("primary")
						.returns(ClassName.BOOLEAN)
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.addStatement("return true")
						.addAnnotation(Override.class)
						.build()
		);
	}
}
