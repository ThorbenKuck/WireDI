package com.wiredi.runtime.retry.backoff;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * A fixed back off is a never changing {@link BackOffStrategy}. The back off will always be the provided {@link #backOff}.
 */
public class FixedBackOffStrategy extends BackOffStrategy<FixedBackOffStrategy> {

	@NotNull
	private final Duration backOff;

	public FixedBackOffStrategy(@NotNull final Duration backOff) {
		this.backOff = backOff;
	}

	@Override
	@NotNull
	protected Duration calculateNext(@NotNull final Duration duration) {
		return backOff;
	}
}
