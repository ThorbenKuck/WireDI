package com.wiredi.runtime.retry;

import com.wiredi.runtime.lang.ThrowingRunnable;
import com.wiredi.runtime.lang.ThrowingSupplier;
import com.wiredi.runtime.retry.backoff.BackOffStrategy;
import com.wiredi.runtime.retry.backoff.ExponentialBackOffStrategy;
import com.wiredi.runtime.retry.backoff.NoBackOffStrategy;
import com.wiredi.runtime.retry.exception.UnsupportedRetryException;
import com.wiredi.runtime.retry.policy.RetryExceptionBarrier;
import com.wiredi.runtime.retry.policy.RetryPolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.wiredi.runtime.lang.Preconditions.isNotNull;

/**
 * This class provides the possibility to execute code and retry on conditions.
 * <p>
 * A RetryTemplate consists of two main parts, the {@link RetryPolicy} and the {@link BackOffStrategy}.
 * The {@link RetryPolicy} describes what to retry. It is responsible for defining outline values for retries.
 * The {@link BackOffStrategy} defines how the delay between retries is defined.
 */
public class RetryTemplate {

    @NotNull
    private final RetryPolicy retryPolicy;

    @NotNull
    private final BackOffStrategy<?> backOffStrategy;

    public RetryTemplate(
            @NotNull final RetryPolicy retryPolicy,
            @NotNull final BackOffStrategy<?> backOffStrategy
    ) {
        this.retryPolicy = retryPolicy;
        this.backOffStrategy = backOffStrategy;
    }

    @NotNull
    public static Builder newInstance() {
        return new Builder();
    }

    /**
     * Retrieves the value from the provided supplier, respecting the {@link RetryPolicy} with the {@link BackOffStrategy}.
     * <p>
     * This function does not support null values. If you require support for null values, use {@link #tryGet(ThrowingSupplier)}.
     *
     * @param supplier the supplier to produce the value
     * @param <T>      The type of the value to extract
     * @return the value returned by the {@link ThrowingSupplier}
     * @see #tryGet(ThrowingSupplier)
     * @see #execute(ThrowingRunnable)
     */
    @NotNull
    public <T, E extends Throwable> T get(@NotNull final ThrowingSupplier<@NotNull T, E> supplier) {
        return isNotNull(doExecute(supplier), () -> "Provided supplier returned null. If null is a valid return value please use tryGet or execute instead.");
    }

    /**
     * Retrieves the value from the provided supplier, respecting the {@link RetryPolicy} with the {@link BackOffStrategy}.
     * <p>
     * The provided supplier may return a null value, contrary to {@link #get(ThrowingSupplier)}.
     *
     * @param supplier the supplier to produce the value
     * @param <T>      The type of the value to extract
     * @return the value returned by the {@link ThrowingSupplier}
     * @see #get(ThrowingSupplier)
     * @see #execute(ThrowingRunnable)
     */
    @Nullable
    public <T, E extends Throwable> T tryGet(@NotNull final ThrowingSupplier<@NotNull T, E> supplier) {
        return doExecute(supplier);
    }

    /**
     * Runs a code, respecting the {@link RetryPolicy} with the {@link BackOffStrategy}.
     *
     * @param runnable the runnable to execute
     * @see #get(ThrowingSupplier)
     * @see #execute(ThrowingRunnable)
     */
    public <E extends Throwable> void execute(@NotNull final ThrowingRunnable<E> runnable) {
        doExecute(ThrowingSupplier.wrap(runnable));
    }

    @Nullable
    private <T, E extends Throwable> T doExecute(@NotNull final ThrowingSupplier<@Nullable T, E> supplier) {
        final RetryState retryState = retryPolicy.newRetryState();
        final RetryExceptionBarrier retryExceptionBarrier = retryPolicy.exceptionBarrier();

        retryState.start();
        while (retryState.isActive()) {
            retryState.sleep();
            try {
                return supplier.get();
            } catch (@NotNull final Throwable throwable) {
                if (retryExceptionBarrier.passes(throwable)) {
                    final Duration nextTimeout = backOffStrategy.next(retryState.timeout());
                    retryState.setNextTimeout(nextTimeout);
                    retryState.addError(throwable);
                } else {
                    retryState.abort();
                    throwable.addSuppressed(new UnsupportedRetryException(throwable));
                }
            }
        }

        return retryState.raiseError();
    }

    public static final class Builder {
        @NotNull
        private RetryPolicy retryPolicy = RetryPolicy.DEFAULT;

        @NotNull
        private BackOffStrategy<?> backOffStrategy = BackOffStrategy.none();

        @NotNull
        public Builder withBackOff(@NotNull final BackOffStrategy<?> backOffStrategy) {
            this.backOffStrategy = backOffStrategy;
            return this;
        }

        @NotNull
        public Builder withFixedBackOff(@NotNull final Duration duration) {
            return withBackOff(BackOffStrategy.fixed(duration));
        }

        @NotNull
        public Builder withFixedBackOff(final long timeout, @NotNull final TimeUnit timeUnit) {
            return withFixedBackOff(Duration.of(timeout, timeUnit.toChronoUnit()));
        }

        @NotNull
        public Builder withLinearBackOff(@NotNull final Duration duration) {
            return withBackOff(BackOffStrategy.fixed(duration));
        }

        @NotNull
        public Builder withLinearBackOff(final long timeout, @NotNull final TimeUnit timeUnit) {
            return withLinearBackOff(Duration.of(timeout, timeUnit.toChronoUnit()));
        }

        @NotNull
        public Builder withExponentialBackoff(final double increment) {
            backOffStrategy = new ExponentialBackOffStrategy(increment);
            return this;
        }

        @NotNull
        public Builder withNoBackoff() {
            backOffStrategy = NoBackOffStrategy.INSTANCE;
            return this;
        }

        @NotNull
        public Builder withRetryPolicy(@NotNull final RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        @NotNull
        public RetryTemplate build() {
            return new RetryTemplate(retryPolicy, backOffStrategy);
        }
    }
}
