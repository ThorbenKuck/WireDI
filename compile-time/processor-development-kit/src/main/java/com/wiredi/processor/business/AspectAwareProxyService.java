package com.wiredi.processor.business;

import com.wiredi.annotations.aspects.AspectTarget;
import com.wiredi.compiler.domain.values.ProxyMethod;
import com.wiredi.processor.AspectIgnoredAnnotations;
import com.wiredi.processor.ProcessorProperties;
import com.wiredi.processor.PropertyKeys;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AspectAwareProxyService {

	private final AspectIgnoredAnnotations aspectIgnoredAnnotations;
	private final ProcessorProperties properties;

	public AspectAwareProxyService(AspectIgnoredAnnotations aspectIgnoredAnnotations, ProcessorProperties properties) {
		this.aspectIgnoredAnnotations = aspectIgnoredAnnotations;
		this.properties = properties;
	}

	public List<ProxyMethod> findEligibleMethods(TypeElement typeElement) {
		return Collections.emptyList();
//		return typeElement.getEnclosedElements()
//				.stream()
//				.filter(it -> it.getKind() == ElementKind.METHOD)
//				.filter(it -> it.getAnnotation(Pure.class) == null)
//				.map(it -> (ExecutableElement) it)
//				.filter(it -> !it.getModifiers().contains(Modifier.PRIVATE))
//				.filter(it -> !it.getModifiers().contains(Modifier.FINAL))
//				// This process can later work, but needs additional work
//				.filter(it -> !it.getModifiers().contains(Modifier.ABSTRACT))
//				.map(it -> new ProxyMethod(it, getAopEnabledAnnotations(it)))
//				.filter(it -> !it.proxyAnnotations().isEmpty())
//				.collect(Collectors.toList());
	}

	public List<AnnotationMirror> getAopEnabledAnnotations(ExecutableElement method) {
		List<AnnotationMirror> result = new ArrayList<>();

		method.getAnnotationMirrors()
				.stream()
				.filter(this::isUsedForAop)
				.forEach(result::add);

		method.getParameters()
				.stream()
				.flatMap(it -> it.getAnnotationMirrors().stream())
				.filter(this::isUsedForAop)
				.forEach(result::add);

		return result;
	}

	public boolean isUsedForAop(AnnotationMirror annotationMirror) {
		if(isAnnotationIgnored(annotationMirror.getAnnotationType().asElement())) {
			return false;
		}

		if(properties.isEnabled(PropertyKeys.AOP_STRICT_ANNOTATION_TARGET)) {
			return annotationMirror.getAnnotationType().asElement().getAnnotation(AspectTarget.class) != null;
		}

		return true;
	}

	public boolean isAnnotationIgnored(Element element) {
		return aspectIgnoredAnnotations.isIgnored(element);
	}

	public boolean isAnnotationIgnored(TypeMirror typeMirror) {
		return aspectIgnoredAnnotations.isIgnored(typeMirror);
	}

}
