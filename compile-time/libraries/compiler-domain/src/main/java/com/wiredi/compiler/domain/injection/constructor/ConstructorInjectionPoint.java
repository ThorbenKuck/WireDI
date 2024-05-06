package com.wiredi.compiler.domain.injection.constructor;

import com.wiredi.compiler.domain.injection.InjectionPoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

public record ConstructorInjectionPoint(
		@NotNull ExecutableElement constructor,
		@NotNull List<ConstructorInjectionParameter> parameters
) implements InjectionPoint {

	public static ConstructorInjectionPoint resolve(ExecutableElement constructor) {
		if (constructor.getKind() != ElementKind.CONSTRUCTOR) {
			throw new IllegalArgumentException("Only constructors can be used for ConstructorInjectionPoints");
		}

		return new ConstructorInjectionPoint(
				constructor,
				constructor.getParameters()
				.stream()
				.map(it -> new ConstructorInjectionParameter(
						it,
						findBackingField(constructor, it),
						findBackingSetter(constructor, it)
				))
				.toList()
		);
	}

	@Nullable
	public static VariableElement findBackingField(
			@NotNull ExecutableElement constructor,
			@NotNull VariableElement parameter
	) {
		return constructor.getEnclosingElement()
				.getEnclosedElements()
				.stream()
				.filter(it -> it.getKind() == ElementKind.FIELD)
				.map(it -> (VariableElement) it)
				.filter(it -> it.getSimpleName().equals(parameter.getSimpleName()))
				.findFirst()
				.orElse(null);
	}

	@Nullable
	public static ExecutableElement findBackingSetter(
			@NotNull ExecutableElement constructor,
			@NotNull VariableElement parameter
	) {
		String parameterName = parameter.getSimpleName().toString();
		String capitalized = parameterName.substring(0, 1).toUpperCase() + parameterName.substring(1);

		return constructor.getEnclosingElement()
				.getEnclosedElements()
				.stream()
				.filter(it -> it.getKind() == ElementKind.METHOD)
				.map(it -> (ExecutableElement) it)
				.filter(it -> it.getSimpleName().toString().equals("set" + capitalized))
				.findFirst()
				.orElse(null);
	}

}
