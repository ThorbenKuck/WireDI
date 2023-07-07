package com.wiredi.processor.business;

import com.wiredi.annotations.Constructed;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.TypeUtils;
import com.wiredi.compiler.domain.injection.*;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.processor.lang.utils.TypeElements;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class InjectionPointService {

	private final TypeElements typeElements;
	private final Elements elements;
	private static final Logger logger = Logger.get(InjectionPointService.class);

	public InjectionPointService(TypeElements typeElements, Elements elements) {
		this.typeElements = typeElements;
		this.elements = elements;
	}

	public InjectionPoints injectionPoints(TypeElement typeElement) {
		return new InjectionPoints(
				findFieldInjectionPoints(typeElement),
				findMethodInjectionPoints(typeElement),
				findConstructorInjectionPoint(typeElement),
				findPostConstructFunctions(typeElement)
		);
	}

	public List<FieldInjectionPoint> findFieldInjectionPoints(TypeElement typeElement) {
		return typeElements.fieldsOf(typeElement)
				.stream()
				.filter(it -> Annotations.hasByName(it, Inject.class))
				.filter(it -> {
					boolean isFinal = it.getModifiers().contains(Modifier.FINAL);
					if (isFinal) {
						logger.warn(it, () -> "This field is marked as an injection point, but final fields cannot be injected!");
					}
					return !isFinal;
				})
				.map(FieldInjectionPoint::new)
				.toList();
	}

	public List<MethodInjectionPoint> findMethodInjectionPoints(TypeElement typeElement) {
		return typeElements.methodsOf(typeElement)
				.stream()
				.filter(it -> Annotations.hasByName(it, Inject.class))
				.map(MethodInjectionPoint::new)
				.toList();
	}

	@Nullable
	public ConstructorInjectionPoint findConstructorInjectionPoint(TypeElement typeElement) {
		return TypeUtils.findPrimaryConstructor(typeElement)
				.map(ConstructorInjectionPoint::new)
				.orElse(null);
	}

	@NotNull
	public List<? extends PostConstructInjectionPoint> findPostConstructFunctions(TypeElement typeElement) {
		return typeElements.methodsOf(typeElement)
				.stream()
				.map(it -> {
					if(Annotations.hasByName(it, PostConstruct.class)) {
						return new PostConstructInjectionPoint(it, false);
					}

					return Annotations.getFrom(it, Constructed.class)
							.map(annotation -> new PostConstructInjectionPoint(it, true))
							.orElse(null);
				})
				.filter(Objects::nonNull)
				.toList();
	}
}
