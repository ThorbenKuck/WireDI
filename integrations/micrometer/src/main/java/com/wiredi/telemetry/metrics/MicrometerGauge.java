package com.wiredi.telemetry.metrics;

import io.micrometer.core.instrument.Gauge;

public class MicrometerGauge<T> implements GaugeType<T> {

    private final T state;
    private final Gauge gauge;

    public MicrometerGauge(T state, Gauge gauge) {
        this.state = state;
        this.gauge = gauge;
    }

    @Override
    public T state() {
        return state;
    }

    @Override
    public double value() {
        return gauge.value();
    }
}
