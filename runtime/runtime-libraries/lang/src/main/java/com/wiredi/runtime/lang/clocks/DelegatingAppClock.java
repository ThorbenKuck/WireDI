package com.wiredi.runtime.lang.clocks;

import com.wiredi.runtime.lang.AppClock;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class DelegatingAppClock implements AppClock {

    private AppClock delegate;

    public DelegatingAppClock(AppClock delegate) {
        this.delegate = delegate;
    }

    @Override
    public long currentTimeMillis() {
        return delegate.currentTimeMillis();
    }

    @Override
    public long nanoTime() {
        return delegate.nanoTime();
    }

    @Override
    public @NotNull ZoneId zoneId() {
        return delegate.zoneId();
    }

    @Override
    public @NotNull Clock toClock() {
        return delegate.toClock();
    }

    @Override
    public @NotNull Instant toInstant() {
        return delegate.toInstant();
    }
}
