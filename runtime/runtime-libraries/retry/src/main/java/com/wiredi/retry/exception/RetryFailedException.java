package com.wiredi.retry.exception;

import com.wiredi.retry.RetryState;

import java.util.Collections;
import java.util.List;

public class RetryFailedException extends RuntimeException {

	private final RetryState retryState;
	private final List<Throwable> errors;

	public RetryFailedException(RetryState retryState, List<Throwable> errors) {
		super("Execution of retry template failed after " + retryState.attempt() + " attempts");
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
