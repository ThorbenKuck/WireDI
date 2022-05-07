package com.github.thorbenkuck.di.processor;

import com.github.thorbenkuck.di.annotations.aspects.AspectTarget;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class Aop {

	private static final AspectIgnoredAnnotations aspectIgnoredAnnotations = AspectIgnoredAnnotations.get();

	public static boolean isAnnotationIgnored(Element element) {
		return aspectIgnoredAnnotations.isIgnored(element);
	}

	public static boolean isAnnotationIgnored(TypeMirror typeMirror) {
		return aspectIgnoredAnnotations.isIgnored(typeMirror);
	}

	public static List<AnnotationMirror> getAopEnabledAnnotations(ExecutableElement method) {
		List<AnnotationMirror> result = new ArrayList<>();

		method.getAnnotationMirrors()
				.stream()
				.filter(Aop::isUsedForAop)
				.forEach(result::add);

		method.getParameters()
				.stream()
				.flatMap(it -> it.getAnnotationMirrors().stream())
				.filter(Aop::isUsedForAop)
				.forEach(result::add);

		return result;
	}

	public static boolean isUsedForAop(AnnotationMirror annotationMirror) {
		if(isAnnotationIgnored(annotationMirror.getAnnotationType().asElement())) {
			return false;
		}

		if(ProcessorProperties.isEnabled(PropertyKeys.AOP_STRICT_ANNOTATION_TARGET)) {
			return annotationMirror.getAnnotationType().asElement().getAnnotation(AspectTarget.class) != null;
		}

		return true;
	}
}
