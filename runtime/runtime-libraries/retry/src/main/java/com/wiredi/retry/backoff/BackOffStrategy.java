package com.wiredi.retry.backoff;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * A BackOff Strategy defines the strategy to increment durations after a retry.
 * <p>
 * Calling {@link #next(Duration)} will calculate the next timeout based on the provided timeout.
 */
public abstract class BackOffStrategy<T extends BackOffStrategy<T>> {

    @Nullable
    private Duration maxTimeout;

    /**
     * Constructs a BackOffStrategy with a fixed timeout value.
     *
     * @param duration the fixed back off
     * @return a new FixedBackOffStrategy
     * @see FixedBackOffStrategy
     * @see #linear(Duration)
     * @see #exponential(double)
     * @see #none()
     */
    @NotNull
    public static FixedBackOffStrategy fixed(@NotNull final Duration duration) {
        return new FixedBackOffStrategy(duration);
    }

    /**
     * Constructs a BackOffStrategy with a fixed timeout increment, resulting in a linear timeout graph.
     *
     * @param increment the increment to add after each timeout
     * @return a new {@link LinearBackOffStrategy}
     * @see LinearBackOffStrategy
     * @see #fixed(Duration)
     * @see #exponential(double)
     * @see #none()
     */
    @NotNull
    public static LinearBackOffStrategy linear(@NotNull final Duration increment) {
        return new LinearBackOffStrategy(increment);
    }

    /**
     * Constructs a BackOffStrategy with a multiplicative timeout multiplier, resulting in an exponential timeout graph.
     *
     * @param multiplier the multiplier to apply to each timeout
     * @return a new {@link ExponentialBackOffStrategy}
     * @see ExponentialBackOffStrategy
     * @see #fixed(Duration)
     * @see #linear(Duration)
     * @see #none()
     */
    @NotNull
    public static ExponentialBackOffStrategy exponential(final double multiplier) {
        return new ExponentialBackOffStrategy(multiplier);
    }

    /**
     * Constructs a BackOffStrategy without any timeout.
     *
     * @return a new {@link NoBackOffStrategy}
     * @see NoBackOffStrategy
     * @see NoBackOffStrategy#INSTANCE
     * @see #fixed(Duration)
     * @see #linear(Duration)
     * @see #exponential(double)
     */
    @NotNull
    public static NoBackOffStrategy none() {
        return NoBackOffStrategy.INSTANCE;
    }

    /**
     * Calculate the next duration based on the current duration.
     * <p>
     * This method should not return null. Instead, implementations should return <pre><code>Duration.ZERO</code></pre>.
     *
     * @param duration the current {@link Duration}
     * @return the next Duration
     */
    @NotNull
    protected abstract Duration calculateNext(@NotNull final Duration duration);

    @NotNull
    public T withMaxTimeout(@NotNull final Duration duration) {
        if (duration.isNegative()) {
            throw new IllegalArgumentException("A max timeout cannot be negative. It should be minimum 0.");
        }

        this.maxTimeout = duration;
        return (T) this;
    }

    /**
     * Calculates the next duration, based on the current duration.
     * <p>
     * The {@link #calculateNext(Duration)} method, which must be overwritten by implementations of this class,
     * will calculate the concrete next duration.
     * The return value is dependent on the implementation.
     * <p>
     * This method will make sure, that the calculated duration is not negative and not higher than the {@link #maxTimeout}.
     * <p>
     * The range of this return value is [0, {@link #maxTimeout}] and can never be null.
     *
     * @param duration the current duration
     * @return the next duration based on the implementation
     */
    @NotNull
    public Duration next(@NotNull final Duration duration) {
        final Duration next = calculateNext(duration);

        if (next.isNegative()) {
            return Duration.ZERO;
        }

        if (maxTimeout != null && next.toNanos() > maxTimeout.toNanos()) {
            return maxTimeout;
        }

        return next;
    }
}
