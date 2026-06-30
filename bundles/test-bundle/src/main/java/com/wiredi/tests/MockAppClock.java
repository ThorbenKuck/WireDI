package com.wiredi.tests;

import com.wiredi.runtime.lang.AppClock;
import com.wiredi.runtime.lang.clocks.DelegatingAppClock;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * A mock clock that can be used for testing purposes.
 */
public class MockAppClock extends DelegatingAppClock {

    @Nullable
    private Long nanos;
    @Nullable
    private ZoneId zoneId = null;

    public MockAppClock(AppClock delegate) {
        super(delegate);
    }

    @Override
    public long currentTimeMillis() {
        if (nanos != null) {
            return TimeUnit.MILLISECONDS.convert(nanos, TimeUnit.NANOSECONDS);
        }
        return super.currentTimeMillis();
    }

    @Override
    public long nanoTime() {
        return Objects.requireNonNullElseGet(nanos, super::nanoTime);
    }

    @Override
    public @NotNull ZoneId zoneId() {
        return Objects.requireNonNullElseGet(zoneId, super::zoneId);
    }

    public MockAppClock set(long amount, @NotNull TimeUnit unit) {
        nanos = unit.toNanos(amount);
        return this;
    }

    public MockAppClock set(@NotNull Duration duration) {
        return set(duration.toNanos(), TimeUnit.NANOSECONDS);
    }

    public MockAppClock add(long amount, @NotNull TimeUnit unit) {
        if (nanos == null) {
            nanos = 0L;
        }
        nanos += unit.toNanos(amount);
        return this;
    }

    public MockAppClock atZone(@NotNull ZoneId zoneId) {
        this.zoneId = zoneId;
        return this;
    }

    public MockAppClock add(@NotNull Duration duration) {
        return add(duration.toNanos(), TimeUnit.NANOSECONDS);
    }

    public MockAppClock addSeconds(long amount) {
        return add(amount, TimeUnit.SECONDS);
    }
}
