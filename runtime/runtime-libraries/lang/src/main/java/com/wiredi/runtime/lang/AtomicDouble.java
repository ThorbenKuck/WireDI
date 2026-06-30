package com.wiredi.runtime.lang;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicDouble extends Number {

    private final AtomicLong value;

    public AtomicDouble() {
        this(0.0);
    }

    public AtomicDouble(double value) {
        this.value = new AtomicLong(Double.doubleToRawLongBits(value));
    }

    @Override
    public int intValue() {
        return (int) get();
    }

    @Override
    public long longValue() {
        return (long) get();
    }

    @Override
    public float floatValue() {
        return (float) get();
    }

    @Override
    public double doubleValue() {
        return get();
    }

    public double get() {
        return Double.longBitsToDouble(value.get());
    }

    public void set(double value) {
        this.value.set(Double.doubleToRawLongBits(value));
    }

    public double increment(double by) {
        while (true) {
            long initialValue = value.get();
            double initialValueDouble = Double.longBitsToDouble(initialValue);
            double newValueDouble = initialValueDouble + by;
            long newValue = Double.doubleToRawLongBits(newValueDouble);

            if (value.compareAndSet(initialValue, newValue)) {
                return newValueDouble;
            }
        }
    }

    public double value() {
        return get();
    }
}
