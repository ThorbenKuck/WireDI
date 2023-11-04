package com.wiredi.retry;

import com.wiredi.retry.exception.RetryInterruptedException;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class RetryState {

	private final long attempt;
	private final long maxAttempts;
	private final Duration timeout;

	private RetryState(
			long attempt,
			long maxAttempts,
			@NotNull Duration timeout
	) {
		this.attempt = attempt;
		this.maxAttempts = maxAttempts;
		this.timeout = timeout;
	}

	public RetryState(long maxAttempts) {
		this(1, maxAttempts, Duration.ZERO);
	}

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
		} catch (InterruptedException e) {
			throw new RetryInterruptedException(e);
		}
	}

	public final RetryState next(Duration duration) {
		return new RetryState(attempt + 1, maxAttempts, duration);
	}

	public final boolean isActive() {
		if (maxAttempts == -1) {
			return true;
		}

		return attempt <= maxAttempts;
	}

	public final boolean isFinished() {
		if (maxAttempts == -1) {
			return false;
		} else {
			return attempt >= maxAttempts;
		}
	}

	public final boolean hasNext() {
		if (maxAttempts == -1) {
			return true;
		}

		return attempt + 1 <= maxAttempts;
	}
}
