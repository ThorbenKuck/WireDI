package com.wiredi.runtime.lang;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowingBiConsumer<T, U, E extends Throwable> {

    static <T, U> ThrowingBiConsumer<T, U, ?> wrap(BiConsumer<T, U> consumer) {
        return consumer::accept;
    }

    void accept(T t, U u) throws E;

    default BiConsumer<T, U> safeSupplier() {
        var parent = this;
        return (t, u) -> {
            try {
                parent.accept(t, u);
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
