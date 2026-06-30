package com.wiredi.telemetry.metrics;

import io.micrometer.core.instrument.Counter;

public class MicrometerCounter implements CounterType {

    private final Counter counter;

    public MicrometerCounter(Counter counter) {
        this.counter = counter;
    }

    @Override
    public double count() {
        return counter.count();
    }

    @Override
    public void increment(double by) {
        counter.increment(by);
    }
}
