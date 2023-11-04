package com.wiredi.domain.conditional.builtin;

import com.wiredi.domain.conditional.ConditionContext;
import com.wiredi.domain.conditional.ConditionEvaluator;
import com.wiredi.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

public class ConditionalOnBeanEvaluator implements ConditionEvaluator {

    @Override
    public boolean matches(
            @NotNull final ConditionContext context
    ) {
        final TypeIdentifier<?> beanType = context.annotationMetaData().requireType("type");
        return context.beanContainer().access(beanType).isNotEmpty();
    }
}
