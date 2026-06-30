package com.wiredi.telemetry.metrics;

import com.wiredi.runtime.lang.ThrowingRunnable;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface TimerType {

    TimerType NOOP = new NoOp();

    static TimerType inMemory() {
        return new InMemoryTimerType();
    }

    void record(long value, @NotNull TimeUnit timeUnit);

    default void record(@NotNull Duration duration) {
        this.record(duration.toNanos(), TimeUnit.NANOSECONDS);
    }

    void record(@NotNull Runnable runnable);

    @Nullable
    <T> T record(@NotNull Supplier<T> supplier);

    default <E extends Throwable> void record(@NotNull ThrowingRunnable<E> runnable) {
        record(runnable.safe());
    }

    @Nullable
    default <T, E extends Throwable> T record(@NotNull ThrowingSupplier<T, E> supplier) {
        return record(supplier.safe());
    }

    @Nullable
    <T> T record(@NotNull Callable<T> callable) throws Exception;

    @NotNull
    default <T, E extends Throwable> Supplier<T> wrap(@NotNull ThrowingSupplier<T, E> supplier) {
        return wrap(supplier.safe());
    }

    @NotNull
    default <T, E extends Throwable> Supplier<T> wrap(@NotNull Supplier<T> supplier) {
        return () -> this.record(supplier);
    }

    @NotNull
    default <E extends Throwable> ThrowingRunnable<E> wrap(@NotNull ThrowingRunnable<E> runnable) {
        return () -> this.record(runnable);
    }

    @NotNull
    default Runnable wrap(@NotNull Runnable runnable) {
        return () -> this.record(runnable);
    }

    @NotNull
    default <T> Callable<T> wrap(@NotNull Callable<T> callable) {
        return () -> this.record(callable);
    }

    long count();

    @NotNull
    Duration totalTime();

    class NoOp implements TimerType {

        @Override
        public void record(long value, @NotNull TimeUnit timeUnit) {

        }

        @Override
        public void record(@NotNull Runnable runnable) {
            runnable.run();
        }

        @Override
        public <T> @Nullable T record(@NotNull Supplier<T> supplier) {
            return supplier.get();
        }

        @Override
        public <T> @Nullable T record(@NotNull Callable<T> callable) throws Exception {
            return callable.call();
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public @NotNull Duration totalTime() {
            return Duration.ZERO;
        }
    }

}
