package com.wiredi.compiler.processor.business;

import com.wiredi.annotations.PrimaryWireType;
import com.wiredi.annotations.Provider;
import com.wiredi.annotations.aspects.Aspect;
import com.wiredi.compiler.domain.Annotations;
import com.wiredi.compiler.domain.annotations.TypedAnnotationSearch;
import com.wiredi.compiler.domain.values.AspectHandlerMethod;
import com.wiredi.compiler.domain.values.FactoryMethod;
import org.slf4j.Logger;import com.wiredi.compiler.processor.lang.utils.TypeElements;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.List;

public class IdentifiableProviderService {

	private final Elements elements;
	private final TypeElements typeElements;

	public IdentifiableProviderService(Elements elements, TypeElements typeElements) {
		this.elements = elements;
		this.typeElements = typeElements;
	}

	public List<FactoryMethod> findAllFactoryMethodsIn(TypeElement typeElement) {
        TypedAnnotationSearch<Provider> providerAnnotation = Annotations.search().byType(Provider.class);

		return typeElements.methodsOf(typeElement)
				.stream()
				.filter(providerAnnotation::isPresentIn)
				.map(it -> new FactoryMethod(typeElement, it))
				.toList();
	}


	public List<AspectHandlerMethod> findAllAspectMethods(TypeElement typeElement) {
        TypedAnnotationSearch<Aspect> aspectAnnotation = Annotations.search().byType(Aspect.class);

        return typeElements.methodsOf(typeElement)
				.stream()
				.filter(aspectAnnotation::isPresentIn)
				.map(it -> new AspectHandlerMethod(typeElement, it, aspectAnnotation.findFirstIn(it).orElseThrow()))
				.toList();
	}

	public TypeMirror getPrimaryWireType(TypeMirror input) {
        return Annotations.search().byType(PrimaryWireType.class)
                .findFirstIn(elements.getTypeElement(input.toString()))
                .map(annotation -> Annotations.extractType(annotation, PrimaryWireType::value))
                .orElse(input);
	}
}
