package com.wiredi.runtime.time.timer.interpreter;

import java.time.Duration;

public class MillisTimeInterpreter implements TimeContext {
    @Override
    public long current() {
        return System.currentTimeMillis();
    }

    @Override
    public long toNanos(long interval) {
        return Duration.ofMillis(interval).toNanos();
    }
}
