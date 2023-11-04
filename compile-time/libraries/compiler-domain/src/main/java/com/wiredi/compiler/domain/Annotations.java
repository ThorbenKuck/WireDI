package com.wiredi.compiler.domain;

import com.wiredi.compiler.logger.Logger;
import com.wiredi.domain.AnnotationMetaData;
import jakarta.inject.Inject;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class Annotations {

	@Inject
	private Elements elements;

	@Inject
	private Types types;

	private static final Logger logger = Logger.get(Annotations.class);

	public static boolean isNotJdkAnnotation(Element element) {
		return !isJdkAnnotation(element);
	}

	public static <T extends Annotation> TypeMirror extractType(T annotation, Function<T, Class<?>> function) {
		try {
			function.apply(annotation);
			throw new IllegalStateException("Impossible");
		} catch (MirroredTypeException e) {
			return e.getTypeMirror();
		}
	}

	public static boolean isJdkAnnotation(Element element) {
		if (element.getKind() != ElementKind.ANNOTATION_TYPE) {
			return false;
		}
		return element.toString().startsWith("java.lang.annotation");
	}

	public static boolean isNotJdkAnnotation(AnnotationMirror annotationMirror) {
		return !isJdkAnnotation(annotationMirror);
	}

	public static boolean isJdkAnnotation(AnnotationMirror annotationMirror) {
		return annotationMirror.getAnnotationType().asElement().toString().startsWith("java.lang.annotation");
	}

	public static boolean isAnnotatedWith(Element element, Class<? extends Annotation> annotation) {
		return getAnnotation(element, annotation).isPresent();
	}

	public static <T extends Annotation> Optional<T> getAnnotation(Element element, Class<T> annotation) {
		return Optional.ofNullable(element.getAnnotation(annotation));
	}

	public static boolean isAnnotatedWith(AnnotationMirror annotationMirror, Class<? extends Annotation> annotation) {
		return getAnnotation(annotationMirror, annotation).isPresent();
	}

	public static <T extends Annotation> Optional<T> getAnnotation(AnnotationMirror annotationMirror, Class<T> annotation) {
		return Optional.ofNullable(annotationMirror.getAnnotationType().asElement().getAnnotation(annotation));
	}

	public static <T extends Annotation> Optional<T> getFrom(Element element, Class<T> annotation) {
		// TODO: Support inherited annotations
		return Optional.ofNullable(element.getAnnotation(annotation));
	}

	public static boolean hasByName(Element element, Class<? extends Annotation> annotation) {
		if (annotation.getAnnotation(Inherited.class) != null) {
			return hasByNameInherited(element, annotation);
		}
		return hasByName(element, annotation.getSimpleName());
	}

	public static boolean hasByName(Element element, String name) {
		return element.getAnnotationMirrors()
				.stream()
				.anyMatch(it -> Objects.equals(it.getAnnotationType().asElement().getSimpleName().toString(), name));
	}

	public static boolean hasByNameInherited(Element element, Class<? extends Annotation> annotation) {
		return hasByNameInherited(element, annotation.getSimpleName());
	}

	public static boolean hasByNameInherited(Element element, String name) {
		if (hasByName(element, name)) {
			return true;
		}
		return element.getAnnotationMirrors()
				.stream()
				.anyMatch(it -> hasByName(it.getAnnotationType(), name));
	}

	public static boolean hasByName(DeclaredType annotation, String name) {
		if (isJdkAnnotation(annotation.asElement())) {
			return false;
		}
		return annotation.getAnnotationMirrors()
				.stream().anyMatch(it -> hasByNameInherited(it.getAnnotationType().asElement(), name));
	}

	public record Result<T extends Annotation>(
			T annotation,
			AnnotationMetaData annotationMetaData
	){}

	public <T extends Annotation> List<Result<T>> findAll(Class<T> annotationType, ClassEntity classEntity) {
		Element element = types.asElement(classEntity.rootType());
		List<Result<T>> result = new ArrayList<>();
		boolean inheritable = annotationType.isAnnotationPresent(Inherited.class);

		for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
			if (isJdkAnnotation(annotationMirror)) {
				continue;
			}

			if (inheritable && isMetaAnnotatedWith(annotationMirror.getAnnotationType().asElement(), annotationType)) {
				T annotation = annotationMirror.getAnnotationType().asElement().getAnnotation(annotationType);
				result.add(new Result<>(annotation, AnnotationMetaData.of(annotationMirror)));
			}

			if (Objects.equals(annotationMirror.getAnnotationType().asElement().toString(), annotationType.getName())) {
				T annotation = element.getAnnotation(annotationType);
				result.add(new Result<>(annotation, AnnotationMetaData.of(annotationMirror)));
			}
		}

		return result;
	}

	public Optional<AnnotationMetaData> findFirst(Class<? extends Annotation> annotation, ClassEntity classEntity) {
		Element element = types.asElement(classEntity.rootType());
		boolean inheritable = annotation.isAnnotationPresent(Inherited.class);

		for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
			if (isJdkAnnotation(annotationMirror)) {
				continue;
			}

			if (inheritable && isMetaAnnotatedWith(annotationMirror.getAnnotationType().asElement(), annotation)) {
				return Optional.of(AnnotationMetaData.of(annotationMirror));
			}

			if (Objects.equals(annotationMirror.getAnnotationType().asElement().toString(), annotation.getName())) {
				return Optional.ofNullable(AnnotationMetaData.of(annotationMirror));
			}
		}

		return Optional.empty();
	}

	public boolean isMetaAnnotatedWith(Element element, Class<? extends Annotation> annotation) {
		for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
			if (Objects.equals(annotationMirror.getAnnotationType().asElement().toString(), annotation.getName())) {
				if (isJdkAnnotation(annotationMirror)) {
					continue;
				}

				return true;
			}
		}

		return false;
	}
}
