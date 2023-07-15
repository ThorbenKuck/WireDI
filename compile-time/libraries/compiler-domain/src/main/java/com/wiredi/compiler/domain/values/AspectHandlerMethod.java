package com.wiredi.compiler.domain.values;

import com.wiredi.annotations.aspects.Aspect;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public record AspectHandlerMethod(
		TypeElement enclosingType,
		ExecutableElement method,
		Aspect annotation
) {
	public TypeMirror returnType() {
		return method.getReturnType();
	}

	public String name() {
		return method.getSimpleName().toString();
	}
}
