package com.wiredi.domain.conditional.builtin;

import com.wiredi.domain.conditional.ConditionContext;
import com.wiredi.domain.conditional.ConditionEvaluator;
import com.wiredi.domain.provider.TypeIdentifier;
import com.wiredi.environment.DefaultEnvironmentKeys;
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
