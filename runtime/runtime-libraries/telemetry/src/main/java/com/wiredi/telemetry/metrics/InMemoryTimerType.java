package com.wiredi.telemetry.metrics;

import com.wiredi.runtime.lang.AppClock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class InMemoryTimerType implements TimerType {

    private final AppClock clock;
    private final AtomicLong nanoSeconds = new AtomicLong();

    public InMemoryTimerType() {
        this(AppClock.system());
    }

    public InMemoryTimerType(AppClock clock) {
        this.clock = clock;
    }

    @Override
    public void record(long value, @NotNull TimeUnit timeUnit) {
        nanoSeconds.addAndGet(timeUnit.toNanos(value));
    }

    @Override
    public void record(@NotNull Duration duration) {
        nanoSeconds.addAndGet(duration.toNanos());
    }

    @Override
    public void record(@NotNull Runnable runnable) {
        final long nano = clock.nanoTime();
        try {
            runnable.run();
        } finally {
            final long e = clock.nanoTime();
            record(e - nano, TimeUnit.NANOSECONDS);
        }

    }

    @Override
    public <T> @Nullable T record(@NotNull Supplier<T> supplier) {
        final long nano = clock.nanoTime();
        try {
            return supplier.get();
        } finally {
            final long e = clock.nanoTime();
            record(e - nano, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public <T> @Nullable T record(@NotNull Callable<T> callable) throws Exception {
        final long nano = clock.nanoTime();
        try {
            return callable.call();
        } finally {
            final long e = clock.nanoTime();
            record(e - nano, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public long count() {
        return nanoSeconds.get();
    }

    @Override
    public @NotNull Duration totalTime() {
        return Duration.ofNanos(nanoSeconds.get());
    }
}
