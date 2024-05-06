package com.wiredi.runtime.time;

import com.wiredi.runtime.lang.ThrowingRunnable;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A content class that represents a time measurement.
 * <p>
 * Constructor function allows you to directly measure how long your code requires.
 * The toString method of this class renders a human-readable string.
 * So by calling toString on an instance of Timed can be directly passed to a logger.
 *
 * <pre><code>
 * public void myMethod() {
 *     var time = Timed.of(() -> {
 *         executeCode();
 *     });
 *
 *     logger.info(() -> "Execution of executeCode took " + time);
 * }
 * </code></pre>
 * <p>
 * Additionally, you can directly chain method calls, like this:
 *
 * <pre><code>
 * Timed.of(() -> {
 *     executeCode();
 * }).then(time -> "Execution of executeCode took " + time);
 * </code></pre>
 */
public class Timed {

    /**
     * A Timed instance that holds 0.
     * <p>
     * This field can be used to hold a single Timed instance, instead of creating a new one every time.
     * It commonly is used, if Timed representation is required (like in the {@link TimedValue#just(Object)}
     */
    @NotNull
    public static final Timed ZERO = new Timed(0);
    private final long nanoseconds;

    /**
     * Constructs a new Time instance representing the provided {@link #nanoseconds} passed.
     *
     * @param nanoseconds the nanoseconds that elapsed
     */
    public Timed(long nanoseconds) {
        if (nanoseconds < 0) {
            throw new IllegalArgumentException("Tried to construct a TimedValue with " + nanoseconds + " nanoseconds, but negative values are not allowed!");
        }
        this.nanoseconds = nanoseconds;
    }

    /**
     * Constructs a new Time instance representing the provided duration passed.
     * <p>
     * The duration will be converted to nanoseconds.
     *
     * @param duration the duration that elapsed
     */
    public Timed(@NotNull final Duration duration) {
        this(duration.toNanos());
    }

    /**
     * A unified static instance method, to construct a new timed instance based on the passed duration
     *
     * @param duration the duration that elapsed
     * @return a new duration instance
     * @see Timed#Timed(Duration)
     */
    @NotNull
    public static Timed of(@NotNull final Duration duration) {
        if (duration.isZero()) {
            return Timed.ZERO;
        }
        return new Timed(duration);
    }

    /**
     * Constructs a new Timed instance, measuring the time the runnable required to execute.
     * <p>
     * The returned timed value holds the nano time that was elapsed while the runnable was executed.
     * <p>
     * Any exception thrown by the provided supplier will be directly forwarded, as specified by the
     * {@link ThrowingRunnable} interface.
     *
     * @param runnable the runnable for which the execution time should be measured
     * @param <E>      a generic for potentially thrown Exception (can be inferred by the runnable)
     * @return a new Timed instance with the elapsed nanoseconds
     * @throws E if the supplier throws the specified exception
     * @see ThrowingRunnable
     */
    @NotNull
    public static <E extends Throwable> Timed of(@NotNull final ThrowingRunnable<E> runnable) throws E {
        final long startNanoseconds = System.nanoTime();
        runnable.run();
        return new Timed(System.nanoTime() - startNanoseconds);
    }

    /**
     * Constructs a new {@link TimedValue} based on the time the supplier required to construct the value.
     * <p>
     * This method delegates to the {@link TimedValue#get(ThrowingSupplier)} and exists for ease of use.
     * For detailed explanations, please see {@link TimedValue#get(ThrowingSupplier)}.
     *
     * @param supplier the supplier for which the time execution should be measured.
     * @param <T>      the type for the constructed value (can be inferred by the supplier)
     * @param <E>      a generic for potentially thrown Exception (can be inferred by the supplier)
     * @return a new TimedValue, holding the constructed value and the required nano execution.
     * @throws E if the supplier throws the specified exception
     * @see TimedValue#get(ThrowingSupplier)
     * @see TimedValue
     */
    @NotNull
    public static <T, E extends Throwable> TimedValue<T> of(@NotNull ThrowingSupplier<T, E> supplier) throws E {
        return TimedValue.get(supplier);
    }

    /**
     * Constructs a new timed instance, that holds the sum of these instances nanoseconds plus the parameters nanoseconds.
     * <p>
     * The calculation will throw an ArithmeticException if the sum is greater than {@link Long#MAX_VALUE}.
     *
     * @param timed the value to add to this one.
     * @return a new instance of {@link Timed}, which holds the sum of both timed instances
     * @see Long#MAX_VALUE
     * @see Math#addExact(long, long)
     */
    @NotNull
    public Timed plus(@NotNull Timed timed) {
        return new Timed(Math.addExact(nanoseconds, timed.nanoseconds));
    }

    /**
     * Constructs a new timed instance, that holds the sum of these nanoseconds and the parameters nanoseconds.
     * <p>
     * The calculation will throw an ArithmeticException if the sum is greater than {@link Long#MAX_VALUE}.
     *
     * @param duration the duration to add to this {@link Timed instance}.
     * @return a new instance of {@link Timed}, which holds the sum of both timed instances
     * @see Long#MAX_VALUE
     * @see Math#addExact(long, long)
     */
    @NotNull
    public Timed plus(@NotNull Duration duration) {
        return new Timed(Math.addExact(nanoseconds, duration.toNanos()));
    }

    @NotNull
    public Timed plus(long value) {
        return new Timed(Math.addExact(nanoseconds, value));
    }

    public Timed minus(Timed timed) {
        return new Timed(Math.subtractExact(nanoseconds, timed.nanoseconds));
    }

    public Timed minus(@NotNull Duration duration) {
        return new Timed(Math.subtractExact(nanoseconds, duration.toNanos()));
    }

    public Timed minus(long value) {
        return new Timed(Math.subtractExact(nanoseconds, value));
    }

    public Timed dividedBy(Timed timed) {
        return new Timed(Math.divideExact(nanoseconds, timed.nanoseconds));
    }

    public Timed dividedBy(Duration duration) {
        return new Timed(Math.divideExact(nanoseconds, duration.toNanos()));
    }

    public Timed dividedBy(long value) {
        return new Timed(Math.divideExact(nanoseconds, value));
    }

    public Timed multipliedBy(Timed timed) {
        return new Timed(Math.multiplyExact(nanoseconds, timed.nanoseconds));
    }

    public Timed multipliedBy(Duration duration) {
        return new Timed(Math.multiplyExact(nanoseconds, duration.toNanos()));
    }

    public Timed multipliedBy(long value) {
        return new Timed(Math.multiplyExact(nanoseconds, value));
    }

    public boolean isGreaterThan(@NotNull Timed timed) {
        return nanoseconds > timed.nanoseconds;
    }

    public boolean isGreaterThan(@NotNull Duration duration) {
        return nanoseconds > duration.toNanos();
    }

    public boolean isGreaterThan(long nanos) {
        return nanoseconds > nanos;
    }

    public boolean isLessThan(@NotNull Timed timed) {
        return nanoseconds < timed.nanoseconds;
    }

    public boolean isLessThan(@NotNull Duration duration) {
        return nanoseconds < duration.toNanos();
    }

    public boolean isLessThan(long nanos) {
        return nanoseconds < nanos;
    }

    /**
     * Calculates the value for the provided {@link TimeUnit}.
     *
     * @param timeUnit the timeunit for which the value should be calculated
     * @return a long value in the unit of the provided {@link TimeUnit}
     */
    public long get(@NotNull TimeUnit timeUnit) {
        return timeUnit.convert(nanoseconds, TimeUnit.NANOSECONDS);
    }

    /**
     * Returns the hold nanoseconds
     *
     * @return the nanoseconds.
     */
    public long getNanoseconds() {
        return nanoseconds;
    }

    /**
     * Constructs a new {@link TimeRenderer}.
     *
     * @return a new {@link TimeRenderer} instance
     * @see TimeRenderer
     */
    @NotNull
    public TimeRenderer render() {
        return new TimeRenderer(nanoseconds);
    }

    /**
     * Constructs a human-readable string, in the {@link TimeUnit}.
     * <p>
     * This method constructs a new {@link TimeRenderer} to construct the human-readable string
     *
     * @param timeUnit the unit which should be rendered human-readable
     * @return a human-readable string in the {@link TimeUnit}
     */
    @NotNull
    public String toString(@NotNull TimeUnit timeUnit) {
        return render().append(timeUnit).toString();
    }

    /**
     * Accepts a consumer, to consume this instance.
     * <p>
     * This function specifically aims at use cases, in which the {@link Timed} instance is not
     * further required after it was consumed.
     * For example, if you just want to log the time and not use them any further.
     * <pre><code>
     * Timed.of(() -> {
     *     executeCode();
     * }).then(time -> "Execution of executeCode took " + time);
     * </code></pre>
     *
     * @param consumer the consumer to consume the timed instance.
     * @return this instance for potential further processing
     */
    @NotNull
    public Timed then(@NotNull Consumer<Timed> consumer) {
        consumer.accept(this);
        return this;
    }

    /**
     * Prints a human-readable representation of the value.
     *
     * @return a new string.
     * @see TimeRenderer
     */
    @Override
    @NotNull
    public String toString() {
        return new TimeRenderer(nanoseconds)
                .append(TimeUnit.DAYS)
                .append(TimeUnit.HOURS)
                .append(TimeUnit.MINUTES)
                .append(TimeUnit.SECONDS)
                .append(TimeUnit.MILLISECONDS)
                .appendIf(TimeUnit.MICROSECONDS, timeRenderer -> timeRenderer.get(TimeUnit.MILLISECONDS) == 0)
                .appendIf(TimeUnit.NANOSECONDS, timeRenderer -> timeRenderer.get(TimeUnit.MILLISECONDS) == 0)
                .toString();
    }
}
