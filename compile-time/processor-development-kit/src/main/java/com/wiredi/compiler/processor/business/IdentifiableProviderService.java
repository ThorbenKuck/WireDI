package com.wiredi.compiler.processor.business;

import com.wiredi.annotations.PrimaryWireType;
import com.wiredi.annotations.Provider;
import com.wiredi.annotations.aspects.Aspect;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.values.AspectHandlerMethod;
import com.wiredi.compiler.domain.values.FactoryMethod;
import com.wiredi.compiler.logger.Logger;
import com.wiredi.compiler.processor.lang.utils.TypeElements;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.List;

public class IdentifiableProviderService {

	private final Elements elements;
	private final TypeElements typeElements;
	private final Annotations annotations;

	public IdentifiableProviderService(Elements elements, TypeElements typeElements, Annotations annotations) {
		this.elements = elements;
		this.typeElements = typeElements;
		this.annotations = annotations;
	}

	public List<FactoryMethod> findAllFactoryMethodsIn(TypeElement typeElement) {
		return typeElements.methodsOf(typeElement)
				.stream()
				.filter(it -> Annotations.hasByName(it, Provider.class))
				.map(it -> new FactoryMethod(typeElement, it))
				.toList();
	}


	public List<AspectHandlerMethod> findAllAspectMethods(TypeElement typeElement) {
		return typeElements.methodsOf(typeElement)
				.stream()
				.filter(it -> Annotations.hasByName(it, Aspect.class))
				.map(it -> new AspectHandlerMethod(typeElement, it, Annotations.getAnnotation(it, Aspect.class).orElseThrow()))
				.toList();
	}

	public TypeMirror getPrimaryWireType(TypeMirror input) {
		return annotations.findClassFieldFromAnnotation(elements.getTypeElement(input.toString()), PrimaryWireType.class, "value")
				.orElse(input);
	}
}
