package com.wiredi.runtime.lang;

import com.wiredi.runtime.lang.clocks.FixedAppClock;
import com.wiredi.runtime.lang.clocks.SystemAppClock;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

/**
 * A central abstraction for time measurement within the application.
 * <p>
 * This interface provides a unified way to access both wall-clock time and high-resolution monotonic time.
 * <p>
 * By using this abstraction instead of direct calls to {@link java.lang.System}, applications gain
 * significantly better testability, as time can be fixed or manipulated in test environments.
 * <p>
 * Usage of this interface is encouraged over direct calls to {@link Clock} in most cases.
 * It is particularly useful for features requiring nanosecond precision or consistent time offsets
 * across different components.
 * Contrary to {@link Clock}, this interface supports nanosecond precision.
 * <p>
 * If the standard implementation {@link SystemAppClock} is not sufficient for your usecase, you can provide your own
 * implementation of the AppClock.
 * This is generally only required if your target JVM does not support the standard {@link java.lang.System} instance.
 * If you just need to set a fixed time for whatever reason, consider using {@link FixedAppClock}.
 * In the case of tests, the test module provides and configures a custom MockAppClock implementation.
 */
public interface AppClock {

    /**
     * A pre-configured instance of the system clock using the UTC time zone.
     */
    AppClock UTC = new SystemAppClock(ZoneOffset.UTC);

    /**
     * Creates a new system clock using the default time zone of the platform.
     *
     * @return a clock that tracks the current system time.
     */
    @NotNull
    static AppClock system() {
        return system(ZoneId.systemDefault());
    }

    /**
     * Creates a new system clock using the specified time zone.
     *
     * @param zoneId the time zone to be used by the clock.
     * @return a clock that tracks the current system time in the given zone.
     */
    @NotNull
    static AppClock system(@NotNull ZoneId zoneId) {
        if (zoneId == ZoneOffset.UTC) {
            return UTC;
        }

        return new SystemAppClock(zoneId);
    }

    /**
     * Creates a clock that always returns the same fixed point in time.
     * <p>
     * This is primarily intended for testing scenarios where deterministic behavior is required.
     *
     * @param zoneId the time zone for the fixed clock.
     * @param millis the fixed epoch milliseconds.
     * @param nanos  the fixed high-resolution nanoseconds.
     * @return a clock that never advances.
     */
    @NotNull
    static AppClock fixed(@NotNull ZoneId zoneId, long millis, long nanos) {
        return new FixedAppClock(zoneId, millis, nanos);
    }

    /**
     * Creates a fixed clock using the system default time zone.
     *
     * @param millis the fixed epoch milliseconds.
     * @param nanos  the fixed high-resolution nanoseconds.
     * @return a clock that never advances.
     */
    @NotNull
    static AppClock fixed(long millis, long nanos) {
        return new FixedAppClock(ZoneId.systemDefault(), millis, nanos);
    }

    /**
     * Creates a fixed clock for the specified time zone and milliseconds.
     * <p>
     * The nanosecond value is automatically calculated from the provided milliseconds.
     *
     * @param zoneId the time zone for the fixed clock.
     * @param millis the fixed epoch milliseconds.
     * @return a clock that never advances.
     */
    @NotNull
    static AppClock fixed(@NotNull ZoneId zoneId, long millis) {
        return fixed(zoneId, millis, TimeUnit.MILLISECONDS.toNanos(millis));
    }

    /**
     * Creates a fixed clock using the system default time zone for the specified milliseconds.
     *
     * @param millis the fixed epoch milliseconds.
     * @return a clock that never advances.
     */
    @NotNull
    static AppClock fixed(long millis) {
        return fixed(ZoneId.systemDefault(), millis);
    }

    /**
     * Converts a standard Java {@link Clock} into a fixed AppClock.
     *
     * @param clock the source clock to capture the current state from.
     * @return a fixed clock based on the current state of the provided clock.
     */
    @NotNull
    static AppClock fixed(@NotNull Clock clock) {
        return fixed(clock.getZone(), clock.millis());
    }

    /**
     * Creates a fixed clock from an {@link Instant} using the system default time zone.
     *
     * @param instant the instant to fix the clock at.
     * @return a fixed clock representing the given instant.
     */
    @NotNull
    static AppClock fixed(@NotNull Instant instant) {
        return fixed(ZoneId.systemDefault(), instant.toEpochMilli(), instant.getNano());
    }

    /**
     * Creates a fixed clock from an {@link Instant} using the specified time zone.
     *
     * @param zoneId  the time zone for the fixed clock.
     * @param instant the instant to fix the clock at.
     * @return a fixed clock representing the given instant.
     */
    @NotNull
    static AppClock fixed(@NotNull ZoneId zoneId, @NotNull Instant instant) {
        return fixed(zoneId, instant.toEpochMilli(), instant.getNano());
    }

    /**
     * Returns the current wall-clock time in milliseconds since the epoch.
     *
     * @return the current time in milliseconds.
     */
    long currentTimeMillis();

    /**
     * Returns the current value of the most precise available system timer, in nanoseconds.
     * <p>
     * This value is typically used for measuring elapsed time and is not related to wall-clock time.
     *
     * @return the current monotonic time in nanoseconds.
     */
    long nanoTime();

    /**
     * Returns the time zone used by this clock.
     *
     * @return the zone identifier.
     */
    @NotNull
    ZoneId zoneId();

    /**
     * Adapts this AppClock to a standard {@link java.time.Clock}.
     * <p>
     * This allows interoperability with standard Java Time APIs while maintaining the time
     * context defined by this instance.
     *
     * @return a standard Clock instance.
     */
    @NotNull
    default Clock toClock() {
        return Clock.fixed(toInstant(), zoneId());
    }

    /**
     * Captures the current point in time as an {@link Instant}.
     *
     * @return an Instant representing the current state of this clock.
     */
    @NotNull
    default Instant toInstant() {
        return Instant.ofEpochMilli(currentTimeMillis())
                .atZone(zoneId())
                .toInstant();
    }
}
