package com.wiredi.runtime.lang;

import java.lang.reflect.UndeclaredThrowableException;

public interface ThrowingRunnable<E extends Throwable> {

    static ThrowingRunnable<?> wrap(Runnable runnable) {
        return runnable::run;
    }

	void run() throws E;

    default Runnable safe() {
        var parent = this;
        return () -> {
            try {
                parent.run();
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
