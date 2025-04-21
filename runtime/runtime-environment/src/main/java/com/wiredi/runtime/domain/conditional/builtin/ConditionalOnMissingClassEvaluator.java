package com.wiredi.runtime.domain.conditional.builtin;

import com.wiredi.runtime.domain.conditional.ConditionEvaluator;
import com.wiredi.runtime.domain.conditional.context.ConditionContext;
import org.jetbrains.annotations.NotNull;

/**
 * Evaluator for {@link ConditionalOnMissingClass} annotation.
 * <p>
 * This evaluator checks if a specified class is absent from the classpath.
 * The condition passes if the class is not available, and fails if the class can be found.
 * This is the negation of {@link ConditionalOnClassEvaluator}.
 */
public class ConditionalOnMissingClassEvaluator implements ConditionEvaluator {

    public static ConditionalOnMissingClassEvaluator INSTANCE = new ConditionalOnMissingClassEvaluator();

    @Override
    public void testRuntimeCondition(final @NotNull ConditionContext.Runtime context) {
        final String className = context.annotationMetaData().require("className");
        
        try {
            // Attempt to load the class
            Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            
            // If we get here, the class exists, so the condition fails
            context.failAndStop("Class was found but should be missing: " + className);
        } catch (ClassNotFoundException e) {
            // Class not found, which means the condition passes
            // Do nothing, as this is what we want
        }
    }
}
