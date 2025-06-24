package com.wiredi.runtime.domain.annotations;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

public record ExtractionContext(
        Element annotatedElement,
        AnnotationMirror annotationMirror,
        AnnotationMetadata annotationMetadata
) {
}
