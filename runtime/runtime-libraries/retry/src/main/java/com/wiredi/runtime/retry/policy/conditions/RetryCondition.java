package com.wiredi.runtime.retry.policy.conditions;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface RetryCondition {

    boolean test(@NotNull final Throwable t);

    @NotNull
    default RetryCondition and(@NotNull final RetryCondition other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) && other.test(t);
    }

    @NotNull
    default RetryCondition negate() {
        return (t) -> !test(t);
    }

    @NotNull
    default RetryCondition or(@NotNull final RetryCondition other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) || other.test(t);
    }

    @NotNull
    static RetryCondition not(@NotNull final RetryCondition target) {
        Objects.requireNonNull(target);
        return target.negate();
    }
}
