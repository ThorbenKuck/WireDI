package com.wiredi.domain.provider.condition;

import com.wiredi.domain.AnnotationMetaData;
import com.wiredi.domain.conditional.ConditionEvaluator;

public record LoadConditionEvaluationStage(
        Class<? extends ConditionEvaluator> type,
        AnnotationMetaData annotationMetaData
) {
}