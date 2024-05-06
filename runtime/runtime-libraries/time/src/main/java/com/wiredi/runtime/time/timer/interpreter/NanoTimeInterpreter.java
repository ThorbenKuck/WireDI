package com.wiredi.runtime.time.timer.interpreter;

public class NanoTimeInterpreter implements TimeContext {
    @Override
    public long current() {
        return System.nanoTime();
    }

    @Override
    public long toNanos(long interval) {
        return interval;
    }
}
