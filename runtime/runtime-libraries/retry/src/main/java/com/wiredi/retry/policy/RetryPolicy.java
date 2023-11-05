package com.wiredi.retry.policy;

import com.wiredi.retry.RetryState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.function.Consumer;

import static com.wiredi.lang.Preconditions.require;

/**
 * This policy describes outline values for retries.
 * <p>
 * The values of this policy are used to construct new {@link RetryState}, which is used for actually retrying.
 */
public class RetryPolicy {

    public static RetryPolicy DEFAULT = new RetryPolicy(null, null, Duration.ZERO, new RetryExceptionBarrier());
    @Nullable
    private final Long maxAttempts;
    @Nullable
    private final Duration maxTimeout;
    @NotNull
    private final Duration delay;
    @NotNull
    private final RetryExceptionBarrier exceptionBarrier;

    public RetryPolicy(
            @Nullable final Long maxAttempts,
            @Nullable final Duration maxTimeout,
            @NotNull final Duration delay,
            @NotNull final RetryExceptionBarrier exceptionBarrier
    ) {
        this.maxAttempts = maxAttempts;
        this.maxTimeout = maxTimeout;
        this.delay = delay;
        this.exceptionBarrier = exceptionBarrier;
    }

    public static Builder newInstance() {
        return new Builder();
    }

    @NotNull
    public RetryExceptionBarrier exceptionBarrier() {
        return this.exceptionBarrier;
    }

    @NotNull
    public RetryState newRetryState() {
        return new RetryState(maxAttempts, delay);
    }

    @Nullable
    public Duration getMaxTimeout() {
        return maxTimeout;
    }

    @Nullable
    public Long getMaxAttempts() {
        return this.maxAttempts;
    }

    public static class Builder {

        @Nullable
        private Long maxAttempts = null;
        @Nullable
        private Duration maxTimeout = null;
        @NotNull
        private Duration delay = Duration.ZERO;
        @NotNull
        private RetryExceptionBarrier exceptionBarrier = new RetryExceptionBarrier();

        /**
         * Sets the max attempts that the code can be executed.
         * <p>
         * The amount of attempts is equal to the amount of code executions.
         * Every {@link com.wiredi.retry.RetryTemplate} execution has at least one attempt, which means that the
         * attempt must at least be one.
         * <p>
         * If you care about the retries that can be taken, you can have a look at the {@link #withMaxRetries(long)} method.
         * This specifies the retries the {@link com.wiredi.retry.RetryTemplate} can take.
         *
         * @param maxAttempts the maximum of code executions.
         * @return this builder
         */
        public Builder withMaxAttempts(final long maxAttempts) {
            require(maxAttempts >= 1, () -> "Every RetryPolicy will have to have at least one max attempt, otherwise code won't be executed at all.");
            this.maxAttempts = maxAttempts;
            return this;
        }

        /**
         * Sets the max retries that can be taken.
         * <p>
         * Max retries are equal to the max attempts - 1.
         * Any code execution is called an attempts.
         * This means that every execution after the first attempt is a retry.
         * Therefore, this function is just a utility function to set the max attempts.
         *
         * @param maxRetries the maximum of retries after the first code execution
         * @return this builder
         */
        public Builder withMaxRetries(final long maxRetries) {
            return withMaxAttempts(maxRetries + 1);
        }

        public Builder withIndefiniteAttempts() {
            this.maxAttempts = null;
            return this;
        }

        public Builder withMaxTimeout(final Duration maxTimeout) {
            this.maxTimeout = maxTimeout;
            return this;
        }

        public Builder withoutMaxTimeout() {
            this.maxTimeout = null;
            return this;
        }

        public Builder withDelay(final Duration duration) {
            this.delay = duration;
            return this;
        }

        public Builder configureExceptionBarrier(@NotNull final Consumer<@NotNull RetryExceptionBarrier> consumer) {
            consumer.accept(exceptionBarrier);
            return this;
        }

        public RetryPolicy build() {
            return new RetryPolicy(maxAttempts, maxTimeout, delay, exceptionBarrier);
        }
    }
}
