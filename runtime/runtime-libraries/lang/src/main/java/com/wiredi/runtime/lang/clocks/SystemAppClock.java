package com.wiredi.runtime.lang.clocks;

import com.wiredi.runtime.lang.AppClock;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;

/**
 * A standard implementation of the clock that delegates to the JVM's system timers.
 *
 * @param zoneId The time zone this system clock operates in.
 */
public record SystemAppClock(@NotNull ZoneId zoneId) implements AppClock {

    @Override
    public long currentTimeMillis() {
        return java.lang.System.currentTimeMillis();
    }

    @Override
    public long nanoTime() {
        return java.lang.System.nanoTime();
    }

    @Override
    public @NotNull ZoneId zoneId() {
        return zoneId;
    }
}