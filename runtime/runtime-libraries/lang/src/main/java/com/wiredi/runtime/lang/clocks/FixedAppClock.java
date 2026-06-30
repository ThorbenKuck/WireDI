package com.wiredi.runtime.lang.clocks;

import com.wiredi.runtime.lang.AppClock;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;

/**
 * A fixed clock implementation.
 * <p>
 * This is used when you want to override the system clock for whatever purposes.
 */
public class FixedAppClock implements AppClock {

    private final ZoneId zoneId;
    private final long millis;
    private final long nanos;

    public FixedAppClock(ZoneId zoneId, long millis, long nanos) {
        this.zoneId = zoneId;
        this.millis = millis;
        this.nanos = nanos;
    }

    @Override
    public long currentTimeMillis() {
        return millis;
    }

    @Override
    public long nanoTime() {
        return nanos;
    }

    @Override
    public @NotNull ZoneId zoneId() {
        return zoneId;
    }
}