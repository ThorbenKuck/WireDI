package com.wiredi.retry.policy;

public class BlackListRetryExceptionBarrier extends RetryExceptionBarrier {

	@Override
	public boolean willRetryFor(Exception e) {
		if (entries.isEmpty()) {
			return true;
		}

		for (Class<? extends Throwable> entry : entries) {
			if (e.getClass().isAssignableFrom(entry)) {
				return false;
			}
		}

		return true;
	}
}
