package com.wiredi.processor.plugins;

import com.wiredi.domain.AnnotationMetaData;

import javax.lang.model.type.TypeMirror;

public record ConditionEntry(
        AnnotationMetaData annotationMetaData,
        TypeMirror annotationType
) {
}
