package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.properties.Key;
import org.jetbrains.annotations.NotNull;

public class ConditionalOnEnabledEvaluator implements ConditionEvaluator {

    @Override
    public void test(ConditionEvaluation.@NotNull Context context) {
        String value = context.annotationMetadata().require("value");
        Boolean enabledByDefault = context.annotationMetadata().getBoolean("enabledByDefault", true);
        Key key = Key.format(value).withSuffix("enabled");
        context.noteDependency(value);

        boolean enabled = context.environment().getProperty(key, enabledByDefault);
        if (!enabled) {
            context.negativeMatch("Integration " + value + " is disabled.");
        } else {
            context.positiveMatch("Integration " + value + " is enabled.");
        }
    }
}
