package com.wiredi.domain.provider.condition;

import com.wiredi.domain.AnnotationMetaData;
import com.wiredi.domain.conditional.ConditionContext;
import com.wiredi.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.WireRepository;

import java.util.Optional;

public class SingleLoadCondition extends AbstractLoadCondition {

    private final AnnotationMetaData annotationMetaData;
    private final Class<? extends ConditionEvaluator> evaluatorType;

    public SingleLoadCondition(Class<? extends ConditionEvaluator> evaluatorType, AnnotationMetaData annotationMetaData) {
        this.annotationMetaData = annotationMetaData;
        this.evaluatorType = evaluatorType;
    }

    @Override
    public boolean matches(WireRepository wireRepository) {
        return evaluate(wireRepository, evaluatorType, annotationMetaData);
    }
}
