package com.wiredi.runtime.lang;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class DynamicBuilder<T> {

    private final T t;

    public DynamicBuilder(T t) {
        this.t = t;
    }

    @NotNull
    public static <T> DynamicBuilder<T> of(@NotNull T t) {
        return new DynamicBuilder<>(t);
    }

    public T setup(Consumer<T> consumer) {
        consumer.accept(t);
        return this.t;
    }
}
