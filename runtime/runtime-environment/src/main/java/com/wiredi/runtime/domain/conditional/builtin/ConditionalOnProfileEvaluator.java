package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.conditional.context.ConditionContext;
import org.jetbrains.annotations.NotNull;

public class ConditionalOnProfileEvaluator implements ConditionEvaluator {

    @Override
    public void testRuntimeCondition(ConditionContext.@NotNull Runtime context) {
        final String profile = context.environment().resolve(context.annotationMetaData().require("value"));
        if (!context.environment().activeProfiles().contains(profile)) {
            context.failAndStop("Profile " + profile + " is not activated");
        }
    }
}
