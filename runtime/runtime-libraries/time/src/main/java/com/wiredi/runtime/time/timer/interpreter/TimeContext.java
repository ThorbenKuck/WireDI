package com.wiredi.runtime.time.timer.interpreter;

public interface TimeContext {

    TimeContext NANOS = new NanoTimeInterpreter();

    TimeContext MILLIS = new MillisTimeInterpreter();

    long current();

    long toNanos(long interval);

}
