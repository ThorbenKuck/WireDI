package com.wiredi.metrics;

import com.wiredi.metrics.types.MetricCounter;
import com.wiredi.metrics.types.MetricGauge;
import com.wiredi.metrics.types.MetricTimer;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class NoopMetricCollector implements MetricCollector {
    @Override
    public MetricCounter counter(String name, MetricTag... tags) {
        return new NoopMetricCounter();
    }

    @Override
    public MetricGauge gauge(String name, Supplier<Double> supplier, MetricTag... tags) {
        return new NoopMetricGauge();
    }

    @Override
    public MetricTimer timer(String name, MetricTag... tags) {
        return new NoopMetricTimer();
    }

    private static class NoopMetricCounter implements MetricCounter {
        @Override
        public void increment() {}

        @Override
        public void increment(long amount) {}

        @Override
        public void decrement() {}

        @Override
        public void decrement(long amount) {}

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void close() {}
    }

    private static class NoopMetricGauge implements MetricGauge {
        @Override
        public Iterable<Measurement> measure() {
            return Collections.emptyList();
        }
    }

    private static class NoopMetricTimer implements MetricTimer {
        @Override
        public void record(Duration duration) {}

        @Override
        public void record(long amount, TimeUnit unit) {}

        @Override
        public void record(Runnable runnable) {}

        @Override
        public <T> T record(Supplier<T> supplier) {
            return supplier.get();
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public double totalTime() {
            return 0;
        }

        @Override
        public void close() {}
    }
}
