package com.wiredi.domain.conditional;

import com.wiredi.domain.AnnotationMetaData;
import com.wiredi.environment.Environment;
import com.wiredi.runtime.beans.BeanContainer;
import org.jetbrains.annotations.NotNull;

public record ConditionContext(
        @NotNull Environment environment,
        @NotNull BeanContainer beanContainer,
        @NotNull AnnotationMetaData annotationMetaData
) {
}
