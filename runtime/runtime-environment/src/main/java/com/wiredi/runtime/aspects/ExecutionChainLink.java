package com.wiredi.runtime.aspects;

import org.jetbrains.annotations.Nullable;

public interface ExecutionChainLink {

    ExecutionContext context();

    Object executeRaw();

    ExecutionChainLink prepend(AspectHandler handler);

    @Nullable
    default <S> S execute() {
        Object o = executeRaw();
        if (o == null) {
            return null;
        }
        return (S) o;
    }

    @Nullable
    default <S> S execute(Class<S> type) {
        Object o = executeRaw();
        if (o == null) {
            return null;
        }
        return type.cast(o);
    }
}
