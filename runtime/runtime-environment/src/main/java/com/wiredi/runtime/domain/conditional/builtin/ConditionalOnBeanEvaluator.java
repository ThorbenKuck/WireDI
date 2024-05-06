package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.ConditionContext;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

public class ConditionalOnBeanEvaluator implements ConditionEvaluator {

    public static ConditionalOnBeanEvaluator INSTANCE = new ConditionalOnBeanEvaluator();

    @Override
    public boolean matches(
            @NotNull final ConditionContext context
    ) {
        final TypeIdentifier<?> beanType = context.annotationMetaData().requireType("type");

        return context.beanContainer()
                .access(beanType)
                .isNotEmpty();
    }
}
