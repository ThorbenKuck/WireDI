package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.conditional.context.ConditionContext;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

public class ConditionalOnMissingBeanEvaluator implements ConditionEvaluator {

    @Override
    public void testRuntimeCondition(ConditionContext.@NotNull Runtime context) {
        final TypeIdentifier<?> beanType = context.annotationMetaData().requireType("type");

        if (context.beanContainer()
                .access(beanType)
                .isNotEmpty()) {
            context.failAndStop("Bean of type " + beanType + " is present");
        }
    }
}
