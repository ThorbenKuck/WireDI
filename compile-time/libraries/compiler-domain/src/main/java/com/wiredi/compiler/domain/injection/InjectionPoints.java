package com.wiredi.compiler.domain.injection;

import java.util.List;

public record InjectionPoints(
		List<? extends FieldInjectionPoint> fieldInjections,
		List<? extends MethodInjectionPoint> methodInjections,
		ConstructorInjectionPoint constructorInjectionPoint,
		List<? extends PostConstructInjectionPoint> postConstructInjectionPoints
) {
}
