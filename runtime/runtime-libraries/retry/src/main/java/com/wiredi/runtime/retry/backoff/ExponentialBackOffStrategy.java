package com.wiredi.runtime.retry.backoff;

import com.wiredi.runtime.retry.policy.RetryPolicy;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * The exponential back off strategy is used to achieve longer and longer timeouts.
 * <p>
 * It is useful for code executions that could experience a denial of service if to many executions happen in a very short time.
 * <p>
 * Note: It is important to set an initial delay in the {@link RetryPolicy} when this {@link BackOffStrategy} is used.
 * Otherwise, this {@link BackOffStrategy} will become a {@link NoBackOffStrategy}, as any value multiplied with 0 will remain 0.
 */
public class ExponentialBackOffStrategy extends BackOffStrategy<ExponentialBackOffStrategy> {

    public final double increment;

    public ExponentialBackOffStrategy(final double increment) {
        this.increment = increment;
        if (increment < 0) {
            throw new IllegalArgumentException("An exponential increment cannot be negative");
        }
    }

    @Override
    @NotNull
    protected Duration calculateNext(@NotNull final Duration duration) {
        if (increment == 0) {
            return Duration.ZERO;
        }
        if (increment == 1) {
            return duration;
        }

        long nanos = Math.round(duration.toNanos() * increment);
        return Duration.ofNanos(nanos);
    }
}
