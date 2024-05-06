package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;

public record LoadConditionEvaluationStage(
        Class<? extends ConditionEvaluator> type,
        AnnotationMetaData annotationMetaData
) {
}