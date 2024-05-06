package com.wiredi.runtime.time;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * This class is a state based class for rendering human-readable strings.
 * <p>
 * It holds a running number, that is modified when parts are added to the result of this Renderer.
 * Whenever {@link #append(TimeUnit)} is called, the running number is modified and reduced accordingly.
 * Subsequent calls to {@link #append(TimeUnit)} with the same {@link TimeUnit} will return 0.
 * <p>
 * When the running number is modified, a human-readable representation of the {@link TimeUnit} is added
 * to the result of the Renderer.
 * <p>
 * For an example of how to use this, see {@link Timed#toString()}.
 */
public class TimeRenderer {

    /**
     * This map holds human-readable representations for {@link TimeUnit}.
     * <p>
     * These values are stored in a constant map, to reduce the memory and performance impact
     * of looking up these values.
     * The human-readable names are created once and stored in a hashmap with O(1) lookup.
     * This way they can easily be used later in lookup.
     */
    public static final Map<@NotNull TimeUnit, @NotNull String> HUMAN_READABLE = new HashMap<>();

    static {
        HUMAN_READABLE.put(TimeUnit.DAYS, "d");
        HUMAN_READABLE.put(TimeUnit.HOURS, "h");
        HUMAN_READABLE.put(TimeUnit.MINUTES, "m");
        HUMAN_READABLE.put(TimeUnit.SECONDS, "s");
        HUMAN_READABLE.put(TimeUnit.MILLISECONDS, "ms");
        HUMAN_READABLE.put(TimeUnit.MICROSECONDS, "㎲");
        HUMAN_READABLE.put(TimeUnit.NANOSECONDS, "ns");
    }

    private final long nanoseconds;
    private final StringBuilder result = new StringBuilder();
    private long runningNanoseconds;

    /**
     * Constructs a new TimeRenderer for the nanoseconds.
     *
     * @param nanoseconds the nanoseconds to use for rendering.
     */
    public TimeRenderer(long nanoseconds) {
        this.nanoseconds = nanoseconds;
        this.runningNanoseconds = nanoseconds;
    }

    public TimeRenderer(@NotNull Timed timed) {
        this(timed.getNanoseconds());
    }

    public TimeRenderer(@NotNull Duration duration) {
        this(duration.getNano());
    }

    /**
     * Returns a human-readable representation for the provided {@link TimeUnit}.
     *
     * @param timeUnit the timeunit to translate.
     * @return a human-readable representation for the {@link TimeUnit}
     */
    @NotNull
    public static String humanReadableOf(@NotNull final TimeUnit timeUnit) {
        final String value = HUMAN_READABLE.get(timeUnit);
        if (value == null) {
            throw new IllegalStateException("Outdated library! It looks like the TimeUnit library gained the new " + timeUnit + " which is not yet supported by WireDI. Please update to the newest version and if this does not help open a ticket on the github page.");
        }
        return value;
    }

    /**
     * Converts the initially set nanos to the provided {@link TimeUnit}.
     *
     * @param timeUnit the {@link TimeUnit} to convert to.
     * @return the initial nano value, converted to the {@link TimeUnit}
     */
    public long get(@NotNull final TimeUnit timeUnit) {
        return timeUnit.convert(nanoseconds, TimeUnit.NANOSECONDS);
    }

    /**
     * Converts the remaining running number to the provided {@link TimeUnit}.
     * <p>
     * Initially this method will return the same as {@link #get(TimeUnit)}.
     * As soon as {@link #append(TimeUnit)} or {@link #appendIf(TimeUnit, Predicate)} is called and
     * modified the running number, this method will return a potentially smaller value.
     *
     * @param timeUnit the {@link TimeUnit} to convert to.
     * @return the current running number nano value, converted to the {@link TimeUnit}
     */
    public long getRemaining(@NotNull final TimeUnit timeUnit) {
        return timeUnit.convert(runningNanoseconds, TimeUnit.NANOSECONDS);
    }

    /**
     * Appends a human-readable value to the internal result.
     * <p>
     * It calculates the remaining value of the converter TimeUnit, appends it to the value and reduces
     * the running number by an equal amount.
     * <p>
     * For example: If the running number is equal to 1e+9 (i.e. 1 {@link TimeUnit#SECONDS} and append
     * is called with {@link TimeUnit#MILLISECONDS}, the running number will be reduced by 1e+9 and
     * "1000ms" will be added to the result.
     * <p>
     * It is important to call higher units first. So {@link TimeUnit#HOURS} before {@link TimeUnit#MINUTES}
     * before  {@link TimeUnit#SECONDS} and so on.
     *
     * @param timeUnit the timeunit, for which the running number should be converted to and reduced by.
     * @return this for further processing.
     */
    @NotNull
    public TimeRenderer append(@NotNull final TimeUnit timeUnit) {
        final long done = getRemaining(timeUnit);
        if (done == 0) {
            return this;
        }

        runningNanoseconds -= timeUnit.toNanos(done);

        if (!result.isEmpty()) {
            result.append(", ");
        }

        result.append(done).append(HUMAN_READABLE.get(timeUnit));
        return this;
    }

    /**
     * This functions works the same way as {@link #append(TimeUnit)}.
     * <p>
     * However, it will only call append if the predicate evaluates to true.
     * This means it can be used in a functional style to add TimeUnits conditionally.
     *
     * @param timeUnit              the timeunit, for which the running number should be converted to and reduced by.
     * @param timeRendererPredicate the predicate which should evaluate to true for {@link #append(TimeUnit)} to be called.
     * @return this for further processing
     * @see #append(TimeUnit)
     */
    @NotNull
    public TimeRenderer appendIf(
            @NotNull final TimeUnit timeUnit,
            @NotNull final Predicate<TimeRenderer> timeRendererPredicate
    ) {
        if (timeRendererPredicate.test(this)) {
            append(timeUnit);
        }

        return this;
    }

    /**
     * Returns the current render value.
     *
     * @return the current render value.
     */
    @NotNull
    public String render() {
        return result.toString();
    }

    /**
     * Returns the current render value.
     * <p>
     * This function just delegates to {@link #render()}
     *
     * @return the current render value.
     */
    @Override
    @NotNull
    public String toString() {
        return render();
    }
}
