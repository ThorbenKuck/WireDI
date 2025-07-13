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

	public Provider.SuperTypes superTypes() {
		return Optional.ofNullable(method.getAnnotation(Provider.class))
				.map(Provider::respect)
				.orElse(Provider.SuperTypes.ALL);
	}

	public String name() {
		return method.getSimpleName().toString();
	}
}
