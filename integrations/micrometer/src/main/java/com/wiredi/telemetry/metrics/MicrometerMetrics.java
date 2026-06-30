package com.wiredi.telemetry.metrics;

import com.wiredi.telemetry.TelemetryTag;
import io.micrometer.core.instrument.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.StreamSupport;

public class MicrometerMetrics implements Metrics {

    private final MeterRegistry meterRegistry;
    private final Map<Counter, CounterType> counterCache = new HashMap<>();
    private final Map<Timer, TimerType> timerCache = new HashMap<>();
    private final Map<Gauge, GaugeType<?>> gaugeCache = new HashMap<>();

    public MicrometerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public @NotNull CounterType counter(@NotNull String name, @NotNull Iterable<TelemetryTag> tags) {
        Counter counter = meterRegistry.counter(name, map(tags));
        return counterCache.computeIfAbsent(counter, MicrometerCounter::new);
    }

    @Override
    public @NotNull <T> GaugeType<T> gauge(@NotNull String name, @NotNull Iterable<TelemetryTag> tags, T state, @NotNull ToDoubleFunction<T> function) {
        Gauge gauge = Gauge.builder(name, state, function).tags(map(tags)).register(meterRegistry);
        return (GaugeType<T>) gaugeCache.computeIfAbsent(gauge, g -> new MicrometerGauge<>(state, g));
    }

    @Override
    public @NotNull TimerType timer(@NotNull String name, @NotNull Iterable<TelemetryTag> tags) {
        Timer timer = meterRegistry.timer(name, map(tags));
        return timerCache.computeIfAbsent(timer, MicrometerTimer::new);
    }

    private List<Tag> map(Iterable<TelemetryTag> input) {
        return StreamSupport.stream(input.spliterator(), false)
                .map(tag -> Tag.of(tag.key(), tag.value()))
                .toList();
    }
}
