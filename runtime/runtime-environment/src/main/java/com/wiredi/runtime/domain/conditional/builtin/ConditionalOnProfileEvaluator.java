package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import org.jetbrains.annotations.NotNull;

public class ConditionalOnProfileEvaluator implements ConditionEvaluator {

    @Override
    public void test(ConditionEvaluation.@NotNull Context context) {
        final String profile = context.environment().resolve(context.annotationMetadata().require("value"));
        if (!context.environment().activeProfiles().contains(profile)) {
            context.negativeMatch("Profile \"" + profile + "\" is not activated");
        } else {
            context.positiveMatch("Profile \"" + profile + "\" is activated");
        }
    }
}
