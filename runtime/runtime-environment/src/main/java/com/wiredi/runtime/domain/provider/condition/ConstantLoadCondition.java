package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;

public class ConstantLoadCondition implements LoadCondition {

    private final boolean condition;

    public ConstantLoadCondition(boolean condition) {
        this.condition = condition;
    }

    @Override
    public final boolean matches(WireRepository wireRepository) {
        return condition;
    }

    @Override
    public LoadCondition add(Class<? extends ConditionEvaluator> evaluatorType, AnnotationMetaData annotationMetaData) {
        throw new UnsupportedOperationException("Constant load conditions cannot be modified");
    }

    @Override
    public String toString() {
        return "Just(" + condition + ')';
    }
}
