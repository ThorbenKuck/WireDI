package com.wiredi.retry.policy.conditions;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ThrowableHasType implements RetryCondition {

    private final String type;

    public ThrowableHasType(@NotNull final Class<? extends Throwable> type) {
        this.type = type.getName();
    }

    @Override
    public boolean test(@NotNull final Throwable throwable) {
        return throwable.getClass().getName().equals(type);
    }

    @Override
    @NotNull
    public String toString() {
        return "IfThrowableHasType(" + type + ')';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThrowableHasType that = (ThrowableHasType) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
