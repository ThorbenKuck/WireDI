package com.wiredi.compiler.domain.entities.methods.identifiableprovider;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.wiredi.compiler.domain.AbstractClassEntity;
import com.wiredi.compiler.domain.entities.methods.MethodFactory;

import javax.lang.model.element.Modifier;

public class OrderMethod implements MethodFactory {

	private final int order;

	public OrderMethod(int order) {
		this.order = order;
	}

	@Override
	public void append(TypeSpec.Builder builder, AbstractClassEntity<?> entity) {
		builder.addMethod(
				MethodSpec.methodBuilder("getOrder")
						.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
						.returns(TypeName.INT)
						.addAnnotation(Override.class)
						.addStatement("return $L", order)
						.build()
		);
	}
}
