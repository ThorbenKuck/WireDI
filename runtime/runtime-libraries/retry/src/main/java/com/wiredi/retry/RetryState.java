package com.wiredi.retry;

import com.wiredi.retry.exception.RetryFailedException;
import com.wiredi.retry.exception.RetryInterruptedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.wiredi.lang.Preconditions.notNull;
import static com.wiredi.lang.Preconditions.require;

public class RetryState {

	@Nullable
	private final Long maxAttempts;
	private long attempt;
	@NotNull
	private Duration timeout;
	private boolean active = true;
	@Nullable
	private Long start = null;
	@Nullable
	private Long stop = null;

	@NotNull
	private final List<@NotNull Throwable> errors = new ArrayList<>();

	public RetryState(
			@Nullable final Long maxAttempts,
			@NotNull final Duration timeout
	) {
		this.attempt = 1;
		this.maxAttempts = maxAttempts;
		this.timeout = timeout;
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

	public final void sleep() {
		if (timeout.isZero() || timeout.isNegative()) {
			return;
		}
		try {
			Thread.sleep(timeout.toMillis());
		} catch (@NotNull final InterruptedException e) {
			throw new RetryInterruptedException(e);
		}
	}

	public final void setNextTimeout(@NotNull final Duration duration) {
		long next = attempt + 1;
		if (maxAttempts != null && next > maxAttempts) {
			abort();
		} else {
			attempt = next;
			this.timeout = duration;
		}
	}

	public final boolean isActive() {
		return this.active;
	}

	public void abort() {
		require(this.stop == null, () -> "The RetryState is already exhausted");
		this.stop = System.nanoTime();
		this.active = false;
	}

	public void start() {
		require(this.start == null, () -> "The RetryState is already exhausted");
		this.start = System.nanoTime();
	}

	public Duration totalDuration() {
		notNull(this.stop);
		notNull(this.start);
		return Duration.ofNanos(this.stop - this.start);
	}
}
