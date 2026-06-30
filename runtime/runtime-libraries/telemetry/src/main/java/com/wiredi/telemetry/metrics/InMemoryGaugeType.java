package com.wiredi.telemetry.metrics;

import java.util.function.ToDoubleFunction;

public class InMemoryGaugeType<T> implements GaugeType<T> {

    private final T state;
    private final ToDoubleFunction<T> gauge;

    public InMemoryGaugeType(T state, ToDoubleFunction<T> gauge) {
        this.gauge = gauge;
        this.state = state;
    }

    @Override
    public T state() {
        return state;
    }

    @Override
    public double value() {
        return gauge.applyAsDouble(state);
    }
}
