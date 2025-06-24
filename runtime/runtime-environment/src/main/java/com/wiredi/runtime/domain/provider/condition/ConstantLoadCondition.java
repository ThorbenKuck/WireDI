package com.wiredi.runtime.domain.provider.condition;

import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import org.jetbrains.annotations.NotNull;

public class ConstantLoadCondition implements LoadCondition {

    private final boolean condition;

    public ConstantLoadCondition(boolean condition) {
        this.condition = condition;
    }

    @Override
    public void test(ConditionEvaluation.Context context) {
        if (!condition) {
            context.negativeMatch("Constant load condition is false");
        } else {
            context.positiveMatch("Constant load condition is true");
        }
    }

    @Override
    public @NotNull LoadCondition add(@NotNull Class<? extends ConditionEvaluator> evaluatorType, @NotNull AnnotationMetadata annotationMetaData) {
        throw new UnsupportedOperationException("Constant load conditions cannot be modified");
    }

    @Override
    public String toString() {
        return "Just(" + condition + ')';
    }
}
