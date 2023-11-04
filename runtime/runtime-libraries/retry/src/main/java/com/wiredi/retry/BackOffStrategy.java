package com.wiredi.retry;

import java.time.Duration;

public abstract class BackOffStrategy {

	private Duration maxTimeout;

	protected abstract Duration calculateNext(Duration duration);

	public static BackOffStrategy fixed(Duration duration) {
		return new FixedBackOffStrategy(duration);
	}

	public static BackOffStrategy exponential(double increment) {
		return new ExponentialBackOffStrategy(increment);
	}

	public BackOffStrategy withMaxTimeout(Duration duration) {
		if (duration.isNegative()) {
			throw new IllegalArgumentException("A max timeout cannot be negative. It should be minimum 0.");
		}
		this.maxTimeout = duration;
		return this;
	}

	public Duration next(Duration duration) {
		Duration next = calculateNext(duration);

		if (next.isNegative()) {
			return Duration.ZERO;
		}

		if (maxTimeout != null && next.toNanos() > maxTimeout.toNanos()) {
			return maxTimeout;
		}

		return next;
	}
}
