package com.wiredi.compiler.domain.values;

import com.wiredi.annotations.Provider;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public record FactoryMethod(TypeElement enclosingType, ExecutableElement method) {
	public TypeMirror returnType() {
		return method.getReturnType();
	}

	public boolean isSingleton() {
		return Optional.ofNullable(method.getAnnotation(Provider.class))
				.map(Provider::singleton)
				.orElse(true);
	}

	public String name() {
		return method.getSimpleName().toString();
	}
}
