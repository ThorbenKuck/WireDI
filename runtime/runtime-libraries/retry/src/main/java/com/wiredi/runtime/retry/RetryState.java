package com.wiredi.runtime.retry;

import com.wiredi.runtime.lang.Preconditions;
import com.wiredi.runtime.retry.backoff.BackOffStrategy;
import com.wiredi.runtime.retry.exception.RetryFailedException;
import com.wiredi.runtime.retry.exception.RetryInterruptedException;
import com.wiredi.runtime.time.TimePrecision;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.wiredi.runtime.lang.Preconditions.is;

public class RetryState {

    @Nullable
    private final Long maxAttempts;
    @NotNull
    private final BackOffStrategy<?> backOffStrategy;
    @NotNull
    private final TimePrecision timePrecision;
    @NotNull
    private final List<@NotNull Throwable> errors = new ArrayList<>();
    private long attempt;
    @NotNull
    private Duration timeout;
    private boolean active = true;
    @Nullable
    private Long start = null;
    @Nullable
    private Long stop = null;

    public RetryState(
            @Nullable final Long maxAttempts,
            @NotNull final Duration timeout,
            @NotNull final BackOffStrategy<?> backOffStrategy,
            @NotNull TimePrecision timePrecision
    ) {
        is(!timeout.isNegative(), () -> "Cannot construct a retry state with the negative timeout: " + timeout);
        this.attempt = 1;
        this.maxAttempts = maxAttempts;
        this.timeout = timeout;
        this.backOffStrategy = backOffStrategy;
        this.timePrecision = timePrecision;
    }

    public RetryState(
            @Nullable final Long maxAttempts,
            @NotNull final Duration timeout,
            @NotNull final BackOffStrategy<?> backOffStrategy
    ) {
        this(maxAttempts, timeout, backOffStrategy, TimePrecision.MILLISECONDS);
    }

    public RetryState(
            @Nullable final Long maxAttempts,
            @NotNull final Duration timeout
    ) {
        this(maxAttempts, timeout, BackOffStrategy.none());
    }

    public void addError(@NotNull final Throwable throwable) {
        this.errors.add(throwable);
    }

    @NotNull
    public <T> T raiseError() throws RetryFailedException {
        if (isActive()) {
            abort();
        }
        throw new RetryFailedException(this, errors);
    }

    @NotNull
    public final Duration timeout() {
        return timeout;
    }

    public final long attempt() {
        return attempt;
    }

    public final boolean sleepAndAdvance() {
        if (timeout.isPositive()) {
            try {
                Thread.sleep(timeout.toMillis());
            } catch (@NotNull final InterruptedException e) {
                throw new RetryInterruptedException(e);
            }
        }

        advanceAttempt();
        if (!isActive()) {
            return false;
        }

        this.timeout = backOffStrategy.next(timeout);
        if (timeout.isNegative()) {
            throw new IllegalStateException("BackOffStrategy returned negative timeout! Check the ued back-off strategy implementation " + backOffStrategy);
        }
        return true;
    }

    private void advanceAttempt() {
        attempt++;
        if (maxAttempts != null && this.attempt > maxAttempts) {
            abort();
        }
    }

    public final boolean isIndefinite() {
        return maxAttempts != null;
    }

    public final boolean isLastAttempt() {
        return maxAttempts != null && attempt == maxAttempts;
    }

    public final boolean isActive() {
        return this.active;
    }

    public void abort() {
        is(this.stop == null, () -> "The RetryState is already exhausted");
        this.stop = timePrecision.now();
        this.active = false;
    }

    public void start() {
        is(this.start == null, () -> "The RetryState is already exhausted");
        this.start = timePrecision.now();
    }

    public Duration totalDuration() {
        Preconditions.isNotNull(this.stop);
        Preconditions.isNotNull(this.start);
        return timePrecision.toDuration(this.start, this.stop);
    }
}
