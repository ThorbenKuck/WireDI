package com.wiredi.telemetry.metrics;

import com.wiredi.telemetry.TelemetryTag;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.function.ToDoubleFunction;

public interface Metrics {

    Metrics NOOP = new NoOp();

    static Metrics inMemory() {
        return new InMemoryMetrics();
    }

    @NotNull
    default CounterType counter(@NotNull String name) {
        return counter(name, Collections.emptyList());
    }

    @NotNull
    CounterType counter(@NotNull String name, @NotNull Iterable<TelemetryTag> tags);

    @NotNull
    default <T extends Number> GaugeType<T> gauge(@NotNull String name, @NotNull T number) {
        return gauge(name, Collections.emptyList(), number);
    }

    @NotNull
    default <T extends Number> GaugeType<T> gauge(@NotNull String name, @NotNull Iterable<TelemetryTag> tags, @NotNull T number) {
        return gauge(name, tags, number, Number::doubleValue);
    }

    @NotNull
    default <T extends Number> GaugeType<T> gauge(@NotNull String name, T state, @NotNull ToDoubleFunction<T> function) {
        return gauge(name, Collections.emptyList(), state, function);
    }

    @NotNull
    <T> GaugeType<T> gauge(@NotNull String name, @NotNull Iterable<TelemetryTag> tags, T state, @NotNull ToDoubleFunction<T> function);

    @NotNull
    default TimerType timer(@NotNull String name) {
        return timer(name, Collections.emptyList());
    }

    @NotNull
    TimerType timer(@NotNull String name, @NotNull Iterable<TelemetryTag> tags);

    class NoOp implements Metrics {

        @Override
        public @NotNull CounterType counter(@NotNull String name, @NotNull Iterable<TelemetryTag> tags) {
            return CounterType.NOOP;
        }

        @Override
        public @NotNull <T> GaugeType<T> gauge(@NotNull String name, @NotNull Iterable<TelemetryTag> tags, T state, @NotNull ToDoubleFunction<T> function) {
            return new GaugeType.NoOp<>(state);
        }

        @Override
        public @NotNull TimerType timer(@NotNull String name, @NotNull Iterable<TelemetryTag> tags) {
            return TimerType.NOOP;
        }
    }

}
