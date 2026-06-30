package com.wiredi.telemetry.metrics;

public interface CounterType {

    NoOp NOOP = new NoOp();

    static CounterType inMemory() {
        return new InMemoryCounterType();
    }

    double count();

    void increment(double by);

    default void increment(int by) {
        increment((double) by);
    }

    default void increment(long by) {
        increment((double) by);
    }

    default void increment(float by) {
        increment((double) by);
    }

    class NoOp implements CounterType {

        @Override
        public double count() {
            return 0;
        }

        @Override
        public void increment(double by) {
        }
    }

}
