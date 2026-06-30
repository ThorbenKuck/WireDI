package com.wiredi.telemetry.metrics;

import java.util.function.ToDoubleFunction;

public interface GaugeType<T> {

    static <T> GaugeType<T> inMemory(T state, ToDoubleFunction<T> gauge) {
        return new InMemoryGaugeType<T>(state, gauge);
    }

    T state();

    double value();

    class NoOp<T> implements GaugeType<T> {

        private final T state;

        public NoOp(T state) {
            this.state = state;
        }

        @Override
        public T state() {
            return state;
        }

        @Override
        public double value() {
            return 0;
        }
    }
}
