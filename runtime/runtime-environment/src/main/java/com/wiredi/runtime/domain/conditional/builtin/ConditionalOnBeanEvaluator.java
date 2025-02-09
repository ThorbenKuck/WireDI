package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.conditional.context.ConditionContext;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

public class ConditionalOnBeanEvaluator implements ConditionEvaluator {

    public static ConditionalOnBeanEvaluator INSTANCE = new ConditionalOnBeanEvaluator();

    @Override
    public void testRuntimeCondition(final @NotNull ConditionContext.Runtime context) {
        final TypeIdentifier<?> beanType = context.annotationMetaData().requireType("type");

        if (
                context.beanContainer()
                        .access(beanType)
                        .isEmpty()
        ) {
            context.failAndStop("Missing bean of type " + beanType);
        }
    }
}
