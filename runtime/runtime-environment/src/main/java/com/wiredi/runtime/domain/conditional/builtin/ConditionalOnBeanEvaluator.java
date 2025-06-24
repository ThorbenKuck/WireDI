package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

public class ConditionalOnBeanEvaluator implements ConditionEvaluator {

    public static ConditionalOnBeanEvaluator INSTANCE = new ConditionalOnBeanEvaluator();

    @Override
    public void test(ConditionEvaluation.@NotNull Context context) {
        final TypeIdentifier<?> beanType = context.annotationMetadata().requireType("type");

        if (!context.wireRepository().contains(beanType)) {
            context.negativeMatch("Missing bean of type " + beanType);
        } else {
            context.positiveMatch("Bean of type " + beanType + " is present");
        }
    }
}
