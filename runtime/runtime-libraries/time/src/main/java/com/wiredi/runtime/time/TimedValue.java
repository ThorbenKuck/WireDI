package com.wiredi.runtime.time;

import com.wiredi.runtime.lang.ThrowingRunnable;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * This class is a wrapper for a value, combining it with the time that was required to construct it.
 * <p>
 * In general, TimedValue are only used for NotNull values.
 *
 * @param value the value
 * @param time  the time that was required to construct it.
 * @param <T>   the generic type of the value
 * @see Timed
 */
public record TimedValue<T>(@NotNull T value, @NotNull Timed time) {

    /**
     * Constructs a new TimedValue from the provided supplier.
     * <p>
     * This method calculates the time based on the System.nano time.
     *
     * @param supplier the supplier to construct the value
     * @param <T>      the generic type of the value (can be inferred by the supplier)
     * @param <E>      any potential exception that might be raised (can be inferred by the supplier)
     * @return a new TimedValue instance that holds value and construction time
     * @throws E any potential exception
     * @see Timed
     * @see Timed#of(ThrowingSupplier)
     * @see Timed#of(ThrowingRunnable)
     */
    @NotNull
    public static <T, E extends Throwable> TimedValue<T> get(@NotNull final ThrowingSupplier<T, E> supplier) throws E {
        return get(supplier, TimePrecision.NANOS);
    }


    /**
     * Constructs a new TimedValue from the provided supplier.
     * <p>
     * This method calculates the time based on the System.nano time.
     *
     * @param supplier the supplier to construct the value
     * @param precision the precision used to measure the execution time.
     * @param <T>      the generic type of the value (can be inferred by the supplier)
     * @param <E>      any potential exception that might be raised (can be inferred by the supplier)
     * @return a new TimedValue instance that holds value and construction time
     * @throws E any potential exception
     * @see Timed
     * @see Timed#of(ThrowingSupplier)
     * @see Timed#of(ThrowingRunnable)
     */
    @NotNull
    public static <T, E extends Throwable> TimedValue<T> get(@NotNull final ThrowingSupplier<T, E> supplier, TimePrecision precision) throws E {
        final long start = precision.now();
        final T result = supplier.get();
        final long elapsed = precision.calculateElapsedNanos(start);
        return new TimedValue<>(result, new Timed(elapsed));
    }

    /**
     * Constructs a new TimedValue, without any specific time.
     * <p>
     * The {@link Timed} will always be {@link Timed#ZERO}
     *
     * @param value the value to wrap
     * @param <T>   the generic type of the value
     * @return a new TimedValue wrapping the value and with Timed being zero.
     */
    @NotNull
    public static <T> TimedValue<T> just(@NotNull T value) {
        return new TimedValue<>(value, Timed.ZERO);
    }

    /**
     * Accepts a consumer, to consume this instance.
     * <p>
     * This function specifically aims at use cases, in which the {@link TimedValue} instance is not
     * further required after it was constructed.
     * For example, if you just want to log the value or pass it to another consumer.
     * <pre><code>
     * TimedValue.get(() -> {
     *     executeCode();
     * }).then(timed -> "Constructed " + timed.value() + " in " + timed.time())
     *      .then(timed -> followupConsumer.accept(timed.value));
     * </code></pre>
     *
     * @param consumer the consumer to consume the TimedValue instance.
     * @return this instance for potential further processing
     */
    @NotNull
    public TimedValue<T> then(@NotNull Consumer<@NotNull TimedValue<T>> consumer) {
        consumer.accept(this);
        return this;
    }
}
