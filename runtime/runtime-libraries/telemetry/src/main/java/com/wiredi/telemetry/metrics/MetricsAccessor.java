package com.wiredi.telemetry.metrics;

import org.jetbrains.annotations.NotNull;

public class MetricsAccessor {

    @NotNull
    private static Metrics instance = Metrics.NOOP;

    @NotNull
    public static Metrics get() {
        return instance;
    }

    public static void set(@NotNull Metrics instance) {
        if (instance != Metrics.NOOP) {
            throw new IllegalStateException("Metrics already set. If you really, really require to override this metric, use the method 'forceSet' instead.");
        }
        MetricsAccessor.instance = instance;
    }

    public static void forceSet(@NotNull Metrics instance) {
        MetricsAccessor.instance = instance;
    }
}
