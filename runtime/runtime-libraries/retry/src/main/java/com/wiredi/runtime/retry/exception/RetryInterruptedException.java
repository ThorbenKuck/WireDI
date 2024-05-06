package com.wiredi.runtime.retry.exception;

import org.jetbrains.annotations.NotNull;

public final class RetryInterruptedException extends RetryException {
	public RetryInterruptedException(@NotNull final InterruptedException e) {
		super(e);
	}
}
