package com.wiredi.retry.exception;

public class RetryInterruptedException extends RuntimeException {

	public RetryInterruptedException(InterruptedException e) {
		super(e);
	}

}
