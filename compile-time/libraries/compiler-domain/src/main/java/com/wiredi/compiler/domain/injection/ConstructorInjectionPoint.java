package com.wiredi.compiler.domain.injection;

import javax.lang.model.element.ExecutableElement;

public record ConstructorInjectionPoint(
		ExecutableElement constructor
) implements InjectionPoint {
}
