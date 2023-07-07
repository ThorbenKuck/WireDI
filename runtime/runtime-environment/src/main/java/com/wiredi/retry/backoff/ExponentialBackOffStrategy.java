package com.wiredi.retry.backoff;

import java.time.Duration;

public class ExponentialBackOffStrategy extends BackOffStrategy {

	public final double increment;

	public ExponentialBackOffStrategy(double increment) {
		this.increment = increment;
		if (increment < 0) {
			throw new IllegalArgumentException("An exponential increment cannot be negative");
		}
	}

	@Override
	protected Duration calculateNext(Duration duration) {
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
