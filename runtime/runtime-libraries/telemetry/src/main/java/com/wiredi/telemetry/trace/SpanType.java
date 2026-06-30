package com.wiredi.telemetry.trace;

import com.wiredi.runtime.lang.ThrowingRunnable;
import com.wiredi.runtime.lang.ThrowingSupplier;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public interface SpanType {

    SpanType INVALID = new NoOp();

    /**
     * Make this span active.
     */
    void makeActive();

    /**
     * Closes this span and reports it.
     */
    void close();

    default boolean isInvalid() {
        return this == INVALID;
    }

    default <T, E extends Throwable> ThrowingSupplier<T, E> wrap(ThrowingSupplier<T, E> supplier) {
        return () -> {
            makeActive();
            try {
                return supplier.get();
            } finally {
                close();
            }
        };
    }

    default <T> Supplier<T> wrap(Supplier<T> supplier) {
        return () -> {
            makeActive();
            try {
                return supplier.get();
            } finally {
                close();
            }
        };
    }

    default <E extends Throwable> ThrowingRunnable<E> wrap(ThrowingRunnable<E> runnable) {
        return () -> {
            makeActive();
            try {
                runnable.run();
            } finally {
                close();
            }
        };
    }

    default Runnable wrap(Runnable runnable) {
        return () -> {
            makeActive();
            try {
                runnable.run();
            } finally {
                close();
            }
        };
    }

    default <T> Callable<T> wrap(Callable<T> callable) {
        return () -> {
            makeActive();
            try {
                return callable.call();
            } finally {
                close();
            }
        };
    }

    Iterable<SpanTransportation> transportations();

    class NoOp implements SpanType {

        @Override
        public void makeActive() {
        }

        @Override
        public void close() {
        }

        @Override
        public Iterable<SpanTransportation> transportations() {
            return Collections.emptyList();
        }
    }

}
