package com.wiredi.runtime.domain.conditional;

import com.wiredi.runtime.domain.conditional.context.ConditionContext;
import org.jetbrains.annotations.NotNull;

/**
 * A ConditionEvaluator is used to determine if certain conditions have been achieved.
 * <p>
 * The primary use case is to determine if an {@link com.wiredi.runtime.domain.provider.IdentifiableProvider}
 * should be loaded into the {@link com.wiredi.runtime.beans.BeanContainer} of a
 * {@link com.wiredi.runtime.WireRepository} or if it should be ignored.
 * <p>
 * ConditionEvaluators are part of the first level api.
 * It is mainly exposed in the {@link Conditional} annotation.
 * <p>
 * Instances of this class are most notably included in the {@link com.wiredi.runtime.domain.provider.condition.LoadCondition}.
 *
 * @see Conditional
 * @see com.wiredi.runtime.domain.provider.condition.LoadCondition
 * @see com.wiredi.runtime.domain.provider.IdentifiableProvider
 * @see ConditionContext
 */
public interface ConditionEvaluator {

    /**
     * Checks if the condition is met.
     *
     * @param context the details, helping to determine if the condition should be met.
     * @see ConditionContext
     */
    default void test(@NotNull final ConditionContext context) {
        switch (context) {
            case ConditionContext.Static s -> testStaticCondition(s);
            case ConditionContext.Runtime r -> testRuntimeCondition(r);
        }
    }

    default void testRuntimeCondition(@NotNull final ConditionContext.Runtime context) {
    }

    default void testStaticCondition(@NotNull final ConditionContext.Static context) {
    }
}
