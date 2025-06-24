package com.wiredi.compiler.processor.plugins;

import com.wiredi.runtime.domain.annotations.AnnotationMetadata;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public record ConditionEntry(
        AnnotationMetadata annotationMetaData,
        TypeMirror evaluatorType,
        TypeElement evaluatorTypeElement
) {
}
