package com.wiredi.retry.backoff;

import java.time.Duration;

public class FixedBackOffStrategy extends BackOffStrategy {

	private final Duration increment;

	public FixedBackOffStrategy(Duration increment) {
		this.increment = increment;
	}

	@Override
	protected Duration calculateNext(Duration duration) {
		return duration.plus(increment) ;
	}
}
