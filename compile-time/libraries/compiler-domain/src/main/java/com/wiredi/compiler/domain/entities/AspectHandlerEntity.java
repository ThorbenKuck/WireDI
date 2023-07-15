package com.wiredi.compiler.domain.entities;

import com.squareup.javapoet.TypeSpec;
import com.wiredi.annotations.Wire;
import com.wiredi.compiler.domain.AbstractClassEntity;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class AspectHandlerEntity extends AbstractClassEntity<AspectHandlerEntity> {

	public AspectHandlerEntity(ExecutableElement declaringMethod) {
		super(declaringMethod.getReturnType(), nameOf(declaringMethod));
	}

	private static String nameOf(ExecutableElement element) {
		return element.getEnclosingElement().getSimpleName()
				+ "$"
				+ element.getSimpleName()
				+ "$AspectHandler";
	}

	@Override
	protected TypeSpec.Builder createBuilder(TypeMirror type) {
		return TypeSpec.classBuilder(className)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addAnnotation(Wire.class);
	}
}
