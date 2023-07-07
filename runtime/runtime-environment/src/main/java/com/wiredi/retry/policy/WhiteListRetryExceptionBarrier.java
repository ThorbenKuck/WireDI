package com.wiredi.retry.policy;

public class WhiteListRetryExceptionBarrier extends RetryExceptionBarrier {

	@Override
	public boolean willRetryFor(Exception e) {
		if (entries.isEmpty()) {
			return false;
		}

		for (Class<? extends Throwable> entry : entries) {
			if (e.getClass().isAssignableFrom(entry)) {
				return true;
			}
		}

		return false;
	}
}
