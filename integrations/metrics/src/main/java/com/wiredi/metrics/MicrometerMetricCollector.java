package com.wiredi.metrics;

import com.wiredi.metrics.types.MetricCounter;
import com.wiredi.metrics.types.MetricGauge;
import com.wiredi.metrics.types.MetricTimer;
import io.micrometer.core.instrument.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MicrometerMetricCollector implements MetricCollector {
    private final MeterRegistry registry;

    public MicrometerMetricCollector(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public MetricCounter counter(String name, MetricTag... tags) {
        return new MicrometerCounter(registry.counter(name, convertTags(tags)));
    }

    @Override
    public MetricGauge gauge(String name, Supplier<Double> supplier, MetricTag... tags) {
        Gauge gauge = Gauge
                .builder(name, supplier::get)
                .tags(convertTags(tags))
                .register(registry);

        return new MicrometerGauge(gauge);
    }

    @Override
    public MetricTimer timer(String name, MetricTag... tags) {
        return new MicrometerTimer(registry.timer(name, convertTags(tags)));
    }

    private Iterable<Tag> convertTags(MetricTag... tags) {
        return Arrays.stream(tags)
                .map(metricTag -> Tag.of(metricTag.key(), metricTag.value()))
                .collect(Collectors.toList());
    }

    private record MicrometerCounter(Counter counter) implements MetricCounter {

        @Override
        public void increment() {
            counter.increment();
        }

        @Override
        public void increment(long amount) {
            counter.increment(amount);
        }

        @Override
        public void decrement() {
            counter.increment(-1);
        }

        @Override
        public void decrement(long amount) {
            counter.increment(-amount);
        }

        @Override
        public long count() {
            return (long) counter.count();
        }

        @Override
        public void close() {
            counter.close();
        }
    }

    private record MicrometerGauge(Gauge gauge) implements MetricGauge {
        @Override
        public Iterable<MicrometerMeasurement> measure() {
            return StreamSupport.stream(gauge.measure().spliterator(), false)
                    .map(MicrometerMeasurement::new)
                    .toList();
        }

        private record MicrometerMeasurement(
                io.micrometer.core.instrument.Measurement measurement
        ) implements Measurement {

            @Override
            public double getValue() {
                return measurement.getValue();
            }
        }
    }

    private record MicrometerTimer(Timer timer) implements MetricTimer {

        @Override
        public void record(Duration duration) {
            timer.record(duration);
        }

        @Override
        public void record(long amount, TimeUnit unit) {
            timer.record(amount, unit);
        }

        @Override
        public void record(Runnable runnable) {
            timer.record(runnable);
        }

        @Override
        public <T> T record(Supplier<T> supplier) {
            return timer.record(supplier);
        }

        @Override
        public long count() {
            return timer.count();
        }

        @Override
        public double totalTime() {
            return timer.totalTime(TimeUnit.NANOSECONDS);
        }

        @Override
        public void close() {
            timer.close();
        }
    }
}