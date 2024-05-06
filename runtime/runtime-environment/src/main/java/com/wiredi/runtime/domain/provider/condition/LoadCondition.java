package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.conditional.Conditional;

public interface LoadCondition {

    boolean matches(WireRepository wireRepository);

    static LoadCondition just(Class<? extends ConditionEvaluator> evaluatorType) {
        return new SingleLoadCondition(evaluatorType);
    }

    static LoadCondition just(Class<? extends ConditionEvaluator> evaluatorType, AnnotationMetaData annotationMetaData) {
        return new SingleLoadCondition(evaluatorType, annotationMetaData);
    }

    default LoadCondition add(Class<? extends ConditionEvaluator> evaluatorType) {
        return add(evaluatorType, AnnotationMetaData.newInstance(Conditional.class.getSimpleName())
                .withField("value", evaluatorType)
                .build());
    }

    default LoadCondition and(LoadCondition other) {
        return new AllLoadCondition(this, other);
    }

    default LoadCondition or(LoadCondition other) {
        return new AnyLoadCondition(this, other);
    }

    LoadCondition add(Class<? extends ConditionEvaluator> evaluatorType, AnnotationMetaData annotationMetaData);

    LoadCondition TRUE = new ConstantLoadCondition(true);

    LoadCondition FALSE = new ConstantLoadCondition(false);

}
