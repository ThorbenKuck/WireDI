package com.wiredi.retry.policy;

import com.wiredi.retry.RetryState;
import com.wiredi.retry.exception.RetryPolicyException;

public class RetryPolicy {

	private long maxAttempts = -1;
	private RetryExceptionBarrier exceptionBarrier = null;

	public RetryPolicy setRetryExceptionBarrier(RetryExceptionBarrier retryExceptionBarrier) {
		if (this.exceptionBarrier == null) {
			this.exceptionBarrier = retryExceptionBarrier;
		} else if (!exceptionBarrier.getClass().equals(retryExceptionBarrier.getClass())) {
			throw new RetryPolicyException("Tried to override " + exceptionBarrier.getClass().getSimpleName() + " with " + retryExceptionBarrier.getClass().getSimpleName() + " which is not allowed!");
		} else {
			this.exceptionBarrier.addAll(retryExceptionBarrier);
		}

		return this;
	}

	public RetryPolicy retryOnException(Class<? extends Throwable> type) {
		return setRetryExceptionBarrier(new WhiteListRetryExceptionBarrier().add(type));
	}

	public RetryPolicy doNotRetryOnException(Class<? extends Throwable> type) {
		return setRetryExceptionBarrier(new BlackListRetryExceptionBarrier().add(type));
	}

	public RetryPolicy setMaxAttempts(long maxAttempts) {
		this.maxAttempts = maxAttempts;
		return this;
	}

	public RetryState newRetryState() {
		return new RetryState(maxAttempts);
	}
}
