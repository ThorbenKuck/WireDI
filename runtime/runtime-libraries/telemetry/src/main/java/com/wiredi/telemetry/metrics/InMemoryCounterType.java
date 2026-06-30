package com.wiredi.telemetry.metrics;

import com.wiredi.runtime.lang.AtomicDouble;

public class InMemoryCounterType implements CounterType {

    private final AtomicDouble counter = new AtomicDouble();

    @Override
    public double count() {
        return counter.get();
    }

    @Override
    public void increment(double by) {
        counter.increment(by);
    }
}
