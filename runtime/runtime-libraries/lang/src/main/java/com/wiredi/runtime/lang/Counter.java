package com.wiredi.runtime.lang;

public final class Counter {

    private int counter;

    public Counter() {
        this(0);
    }

    public Counter(int counter) {
        this.counter = counter;
    }

    public void reset() {
        this.counter = 0;
    }

    public void set(int counter) {
        this.counter = counter;
    }

    public int get() {
        return this.counter;
    }

    public int increment() {
        return ++this.counter;
    }

    public int increment(int by) {
        this.counter += by;
        return this.counter;
    }

    @Override
    public String toString() {
        return "Counter{" + counter + '}';
    }
}
