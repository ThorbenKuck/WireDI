package com.wiredi.telemetry.metrics;

import com.wiredi.telemetry.TelemetryTag;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToDoubleFunction;

public class InMemoryMetrics implements Metrics {

    private final Map<Key, CounterType> counters = new ConcurrentHashMap<>();
    private final Map<Key, GaugeType<?>> gauges = new ConcurrentHashMap<>();
    private final Map<Key, TimerType> timers = new ConcurrentHashMap<>();

    @Override
    public @NotNull CounterType counter(@NotNull String name, @NotNull Iterable<TelemetryTag> tags) {
        return counters.computeIfAbsent(new Key(name, tags), k -> CounterType.inMemory());
    }

    @Override
    public @NotNull <T> GaugeType<T> gauge(@NotNull String name, @NotNull Iterable<TelemetryTag> tags, T state, @NotNull ToDoubleFunction<T> function) {
        GaugeType<?> gauge = gauges.computeIfAbsent(new Key(name, tags), k -> GaugeType.inMemory(state, function));
        if (gauge.state() != state) {
            throw new IllegalArgumentException("Gauge already exists with different state. Please make sure you are using the same state object for all gauges with the same name and tags.");
        }
        return (GaugeType<T>) gauge;
    }

    @Override
    public @NotNull TimerType timer(@NotNull String name, @NotNull Iterable<TelemetryTag> tags) {
        return timers.computeIfAbsent(new Key(name, tags), k -> TimerType.inMemory());
    }

    private record Key(String name, Iterable<TelemetryTag> tags) {}
}
