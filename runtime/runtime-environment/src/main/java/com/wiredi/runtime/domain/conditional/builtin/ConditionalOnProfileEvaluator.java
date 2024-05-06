package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.ConditionContext;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.environment.DefaultEnvironmentKeys;
import org.jetbrains.annotations.NotNull;

public class ConditionalOnProfileEvaluator implements ConditionEvaluator {

    @Override
    public boolean matches(
            @NotNull final ConditionContext context
    ) {
        final String profile = context.environment().resolve(context.annotationMetaData().require("value"));
        return context.environment().getAllProperties(DefaultEnvironmentKeys.ACTIVE_PROFILES).contains(profile);
    }
}
