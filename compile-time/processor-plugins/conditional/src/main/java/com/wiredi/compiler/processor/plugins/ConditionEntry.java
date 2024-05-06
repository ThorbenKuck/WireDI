package com.wiredi.compiler.processor.plugins;

import com.wiredi.runtime.domain.AnnotationMetaData;

import javax.lang.model.type.TypeMirror;

public record ConditionEntry(
        AnnotationMetaData annotationMetaData,
        TypeMirror annotationType
) {
}
