package com.wiredi.processor;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// TODO: Cache Results to reduce performance footprint
public class AnnotationTypeFieldExtractor {

	public static boolean hasAnnotationByName(Element element, String name) {
		return hasAnnotationByName(element, name);
	}

	public static boolean hasAnnotationByName(Element element, String name, boolean supportInheritance) {
		return findAllAnnotations(element, supportInheritance).stream()
				.anyMatch(it -> it.getAnnotationType().asElement().getSimpleName().toString().equals(name));
	}

	public static boolean hasAnyAnnotationByName(Element element, List<String> names, boolean supportInheritance) {
		return findAllAnnotations(element, supportInheritance).stream()
				.anyMatch(it -> names.contains(it.getAnnotationType().asElement().getSimpleName().toString()));
	}

	public static List<AnnotationMirror> findAllAnnotations(Element element, boolean supportInheritance) {
		final List<AnnotationMirror> result = new ArrayList<>();
		element.getAnnotationMirrors()
				.stream()
				.filter(AnnotationTypeFieldExtractor::isUserAnnotation)
				.forEach(mirror -> {
					result.add(mirror);
					if(supportInheritance) {
						result.addAll(findAllAnnotations(mirror));
					}
				});
		return result;
	}

	public static boolean isRepeatable(AnnotationMirror annotationMirror) {
		return annotationMirror.getAnnotationType()
				.asElement()
				.getAnnotationMirrors()
				.stream()
				.anyMatch(it -> hasName(it, "Repeatable"));
	}

	public static List<AnnotationMirror> findAllAnnotations(AnnotationMirror annotationMirror) {
		final List<AnnotationMirror> result = new ArrayList<>();
		annotationMirror.getAnnotationType().asElement()
				.getAnnotationMirrors()
				.stream()
				.filter(AnnotationTypeFieldExtractor::isUserAnnotation)
				.forEach(mirror -> {
					result.add(mirror);
					result.addAll(findAllAnnotations(mirror));
				});
		return result;
	}

	public static boolean isUserAnnotation(AnnotationMirror mirror) {
		return !isSystemAnnotation(mirror);
	}

	public static boolean isSystemAnnotation(AnnotationMirror mirror) {
		return hasName(mirror, "Target") || hasName(mirror, "Retention") || hasName(mirror, "Documented");
	}

	public static boolean hasName(AnnotationMirror annotationMirror, String name) {
		return annotationMirror.getAnnotationType().asElement().getSimpleName().toString().equals(name);
	}

	public static <T> TypeMirror extractFromClassField(Supplier<Class<T>> supplier) {
		return extractFirstFromException(supplier);
	}

	public static TypeMirror extractFirstFromClassFields(Supplier<Class<?>[]> supplier) {
		return extractFirstFromException(supplier);
	}

	public static List<? extends TypeMirror> extractAllFromClassFields(Supplier<Class<?>[]> supplier) {
		try {
			supplier.get();
			throw new IllegalArgumentException("Invalid Supplier function provided!");
		} catch (javax.lang.model.type.MirroredTypesException e) {
			return e.getTypeMirrors();
		}
	}

	private static TypeMirror extractFirstFromException(Supplier<?> supplier) {
		try {
			supplier.get();
			throw new IllegalArgumentException("Invalid Supplier function provided!");
		} catch (javax.lang.model.type.MirroredTypesException e) {
			List<? extends TypeMirror> typeMirrors = e.getTypeMirrors();
			if (typeMirrors.isEmpty()) {
				throw new IllegalArgumentException("The provided supplier appears to not have return the correct class");
			}

			return typeMirrors.get(0);
		}
	}
}
