package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.conditional.context.ConditionContext;
import org.jetbrains.annotations.NotNull;

/**
 * Evaluator for {@link ConditionalOnClass} annotation.
 * <p>
 * This evaluator checks if a specified class is present in the classpath.
 * The condition passes if the class is available, and fails if the class cannot be found.
 */
public class ConditionalOnClassEvaluator implements ConditionEvaluator {

    public static ConditionalOnClassEvaluator INSTANCE = new ConditionalOnClassEvaluator();

    @Override
    public void testRuntimeCondition(final @NotNull ConditionContext.Runtime context) {
        final String className = context.annotationMetaData().require("className");

        try {
            // Attempt to load the class
            Class.forName(className, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            // Class not found, fail the condition
            context.failAndStop("Required class not found: " + className);
        }
    }
}
