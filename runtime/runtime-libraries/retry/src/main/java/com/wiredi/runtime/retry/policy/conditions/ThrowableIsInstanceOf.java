package com.wiredi.runtime.retry.policy.conditions;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ThrowableIsInstanceOf implements RetryCondition {

    @NotNull
    private final Class<? extends Throwable> type;

    public ThrowableIsInstanceOf(@NotNull final Class<? extends Throwable> type) {
        this.type = type;
    }

    @Override
    public boolean test(@NotNull final Throwable throwable) {
        return type.isAssignableFrom(throwable.getClass());
    }

    @Override
    @NotNull
    public String toString() {
        return "IfThrowableIsInstanceOf(" + type + ')';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThrowableIsInstanceOf that = (ThrowableIsInstanceOf) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
