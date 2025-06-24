package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.Environment;
import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.properties.Key;
import org.jetbrains.annotations.NotNull;

public class ConditionalOnPropertyEvaluator implements ConditionEvaluator {

    @Override
    public void test(ConditionEvaluation.@NotNull Context context) {
        Environment environment = context.environment();

        String key = environment.resolve(context.annotationMetadata().require("key"));
        String havingValue = context.annotationMetadata().get("havingValue").map(environment::resolve).orElse(null);
        boolean matchIfMissing = context.annotationMetadata().getBoolean("matchIfMissing").orElse(false);

        String property = environment.getProperty(Key.format(key));
        if (property == null) {
            if (!matchIfMissing) {
                context.negativeMatch("Missing property '" + key + "'");
            } else {
                context.positiveMatch("Property '" + key + "' is missing");
            }
        } else {
            if (havingValue != null && !havingValue.equals(property)) {
                context.negativeMatch("Property '" + key + "' does not match '" + havingValue + "'");
            } else {
                context.positiveMatch("Property '" + key + "' matches '" + havingValue + "'");
            }
        }
    }
}
