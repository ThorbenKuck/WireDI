package com.wiredi.runtime.retry.exception;

import com.wiredi.runtime.retry.RetryState;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class RetryFailedException extends RetryException {

	@NotNull
	private final RetryState retryState;
	@NotNull
	private final List<Throwable> errors;

	public RetryFailedException(@NotNull final RetryState retryState, @NotNull final List<Throwable> errors) {
		super("Execution of retry template failed after " + (retryState.attempt() -1) + " attempts");
		this.retryState = retryState;
		this.errors = Collections.unmodifiableList(errors);
		errors.forEach(this::addSuppressed);
	}

	public RetryState getRetryState() {
		return retryState;
	}

	public List<Throwable> getErrors() {
		return errors;
	}
}
