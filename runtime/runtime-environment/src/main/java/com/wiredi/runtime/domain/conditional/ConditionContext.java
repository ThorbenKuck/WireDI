package com.wiredi.runtime.domain.conditional;

import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.beans.BeanContainer;
import org.jetbrains.annotations.NotNull;

public record ConditionContext(
        @NotNull Environment environment,
        @NotNull BeanContainer beanContainer,
        @NotNull AnnotationMetaData annotationMetaData
) {
}
