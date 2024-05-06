package com.wiredi.runtime.lang;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.function.Consumer;
import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {

    static <T> ThrowingConsumer<T, ?> wrap(Consumer<T> consumer) {
        return consumer::accept;
    }

    void accept(T t) throws E;

    default Consumer<T> safeSupplier() {
        var parent = this;
        return (it) -> {
            try {
                parent.accept(it);
            } catch (Throwable e) {
                if (e instanceof RuntimeException r) {
                    throw r;
                } else if(e instanceof IOException io) {
                    throw new UncheckedIOException(io);
                } else {
                    throw new UndeclaredThrowableException(e);
                }
            }
        };
    }
}
