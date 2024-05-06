package com.wiredi.runtime.async;

public class AsyncBarrierException extends RuntimeException {
	public AsyncBarrierException(Throwable cause) {
		super(cause);
	}

	public AsyncBarrierException(String message) {
		super(message);
	}
}
