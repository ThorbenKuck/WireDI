package com.wiredi.runtime.domain.annotations;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

public interface ExtractionEnvironment {

    boolean isAnnotatedWith(Element element, Class<? extends Annotation> annotationClass);

    boolean hasAnnotationByName(Element element, String annotationName);

    boolean hasAnnotationByName(Element element, Class<? extends Annotation> annotationClass);

    boolean hasAnnotationByName(DeclaredType declaredType, Class<? extends Annotation> annotationName);

    boolean hasAnnotationByName(DeclaredType declaredType, String annotationName);

    <T extends Annotation> Optional<T> getAnnotation(AnnotationMirror annotationMirror, Class<T> annotation);

    <T extends Annotation> Optional<T> getAnnotation(Element element, Class<T> annotation);

    <T extends Annotation> List<AnnotationExcerpt<T>> findAll(Class<T> annotationType, Element element);

    AnnotationMetadata getAnnotationMetadata(Element annotatedElement, AnnotationMirror annotationMirror);
}
