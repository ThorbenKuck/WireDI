package com.wiredi.runtime.lang;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Throwable> {

    static <T> ThrowingSupplier<T, ?> wrap(Supplier<T> supplier) {
        return supplier::get;
    }

    static <T, E extends Throwable> ThrowingSupplier<T, E> wrap(ThrowingRunnable<E> runnable) {
        return () -> {
            runnable.run();
            return null;
        };
    }

    T get() throws E;

    default Supplier<T> safeSupplier() {
        var parent = this;
        return () -> {
            try {
                return parent.get();
            } catch (Throwable e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new UndeclaredThrowableException(e);
                }
            }
        };
    }
}
