package com.wiredi.runtime.retry.policy;

import com.wiredi.runtime.retry.policy.conditions.RetryCondition;
import com.wiredi.runtime.retry.policy.conditions.ThrowableHasType;
import com.wiredi.runtime.retry.policy.conditions.ThrowableIsInstanceOf;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A RetryException defines which exception should be retried and which not.
 * <p>
 * The barrier itself holds two states: positive and negative matches. For
 */
public class RetryExceptionBarrier {

    @NotNull protected final List<@NotNull RetryCondition> positiveMatches = new ArrayList<>();
    @NotNull protected final List<@NotNull RetryCondition> negativeMatches = new ArrayList<>();
    @NotNull protected RetryCondition defaultCondition = (t) -> positiveMatches.isEmpty();

    @NotNull
    public FluentConfiguration retryIf() {
        return new FluentConfiguration(this::retryIf);
    }

    @NotNull
    public FluentConfiguration doNotRetryIf() {
        return new FluentConfiguration(this::doNotRetryIf);
    }

    @NotNull
    public RetryExceptionBarrier retryIf(@NotNull final RetryCondition predicate) {
        positiveMatches.add(predicate);
        return this;
    }

    @NotNull
    public RetryExceptionBarrier doNotRetryIf(@NotNull final RetryCondition predicate) {
        negativeMatches.add(predicate);
        return this;
    }

    @NotNull
    public RetryExceptionBarrier defaultCondition(@NotNull final RetryCondition retryCondition) {
        this.defaultCondition = retryCondition;
        return this;
    }

    @NotNull
    public RetryExceptionBarrier takeFrom(@NotNull final RetryExceptionBarrier that) {
        this.positiveMatches.addAll(that.positiveMatches);
        this.negativeMatches.addAll(that.negativeMatches);

        return this;
    }

    /**
     * Whether the provided {@link Throwable} is okay to retry.
     * <p>
     * This function will test both the positive and negative matches.
     * <p>
     * The highest precedence have the {@link #negativeMatches} that where provided.
     * Only if no negative match applies will the positive matches be consulted.
     * <p>
     * Lastly, if not a single negative nor a single positive match apply will the {@link #defaultCondition} be consulted.
     * The default condition (if not changed) will pass if no positive matches are provided.
     *
     * @param throwable the throwable to test
     * @return true, if the throwable is okay to retry or false if it is not
     */
    public boolean passes(@NotNull final Throwable throwable) {
        for (@NotNull final RetryCondition retryFor : negativeMatches) {
            if (retryFor.test(throwable)) {
                return false;
            }
        }

        for (@NotNull final RetryCondition retryFor : positiveMatches) {
            if (retryFor.test(throwable)) {
                return true;
            }
        }

        return defaultCondition.test(throwable);
    }

    public static final class FluentConfiguration {

        private final Function<@NotNull RetryCondition, @NotNull RetryExceptionBarrier> parent;

        public FluentConfiguration(@NotNull final Function<RetryCondition, RetryExceptionBarrier> parent) {
            this.parent = parent;
        }

        @NotNull
        public RetryExceptionBarrier throwableHasType(@NotNull final Class<? extends Throwable> type) {
            return parent.apply(new ThrowableHasType(type));
        }

        @NotNull
        public RetryExceptionBarrier throwableIsInstanceOf(@NotNull final Class<? extends Throwable> type) {
            return parent.apply(new ThrowableIsInstanceOf(type));
        }
    }
}
