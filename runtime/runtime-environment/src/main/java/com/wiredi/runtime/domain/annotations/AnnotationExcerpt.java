package com.wiredi.runtime.domain.annotations;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;

public record AnnotationExcerpt<T extends Annotation>(
        T instance,
        AnnotationMirror mirror,
        AnnotationMetadata metadata
) {
}