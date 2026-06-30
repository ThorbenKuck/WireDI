package com.wiredi.telemetry.metrics;

import io.micrometer.core.instrument.Timer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class MicrometerTimer implements TimerType {

    private final Timer timer;

    public MicrometerTimer(Timer timer) {
        this.timer = timer;
    }

    @Override
    public void record(long value, @NotNull TimeUnit timeUnit) {
        timer.record(value, timeUnit);
    }

    @Override
    public void record(@NotNull Runnable runnable) {
        timer.record(runnable);
    }

    @Override
    public <T> @Nullable T record(@NotNull Supplier<T> supplier) {
        return timer.record(supplier);
    }

    @Override
    public <T> @Nullable T record(@NotNull Callable<T> callable) throws Exception {
        return timer.recordCallable(callable);
    }

    @Override
    public long count() {
        return timer.count();
    }

    @Override
    public @NotNull Duration totalTime() {
        return Duration.ofNanos((long) timer.totalTime(TimeUnit.NANOSECONDS));
    }
}
