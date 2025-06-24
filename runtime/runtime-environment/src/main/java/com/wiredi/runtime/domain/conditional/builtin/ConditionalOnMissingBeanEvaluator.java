package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

public class ConditionalOnMissingBeanEvaluator implements ConditionEvaluator {

    @Override
    public void test(ConditionEvaluation.@NotNull Context context) {
        final TypeIdentifier<?> beanType = context.annotationMetadata().requireType("type");

        if (context.wireRepository().contains(beanType)) {
            context.negativeMatch("Bean of type " + beanType + " is present");
        } else {
            context.positiveMatch("Bean of type " + beanType + " is missing");
        }
    }
}
