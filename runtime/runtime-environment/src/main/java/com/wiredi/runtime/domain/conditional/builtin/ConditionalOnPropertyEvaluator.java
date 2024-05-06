package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.ConditionContext;
import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.properties.Key;
import org.jetbrains.annotations.NotNull;

public class ConditionalOnPropertyEvaluator implements ConditionEvaluator {

    @Override
    public boolean matches(
            @NotNull final ConditionContext context
    ) {
        Environment environment = context.environment();

        String key = environment.resolve(context.annotationMetaData().require("key"));
        String havingValue = context.annotationMetaData().get("havingValue").map(environment::resolve).orElse(null);
        boolean matchIfMissing = context.annotationMetaData().getBoolean("matchIfMissing").orElse(false);

        String property = environment.getProperty(Key.format(key));
        if (property == null) {
            return matchIfMissing;
        } else {
            if (havingValue == null) {
                return true;
            } else {
                return property.equals(havingValue);
            }
        }
    }
}
