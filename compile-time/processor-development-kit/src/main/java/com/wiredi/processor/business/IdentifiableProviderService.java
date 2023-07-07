package com.wiredi.processor.business;

import com.wiredi.annotations.PrimaryWireType;
import com.wiredi.annotations.Provider;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.TypeUtils;
import com.wiredi.compiler.domain.values.FactoryMethod;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.List;
import java.util.Optional;

public class IdentifiableProviderService {

	private final Elements elements;

	public IdentifiableProviderService(Elements elements) {
		this.elements = elements;
	}

	public List<FactoryMethod> findAllFactoryMethodsIn(TypeElement typeElement) {
		return typeElement.getEnclosedElements()
				.stream()
				.filter(it -> it.getKind() == ElementKind.METHOD && Annotations.hasByName(it, Provider.class))
				.map(it -> new FactoryMethod(typeElement, (ExecutableElement) it))
				.toList();
	}


	public TypeMirror getPrimaryWireType(TypeMirror input) {
		return Optional.ofNullable(input.getAnnotation(PrimaryWireType.class))
				.map(it -> elements.getTypeElement(it.value().getName()).asType())
				.orElse(input);
	}
}
