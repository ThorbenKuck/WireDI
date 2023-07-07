package com.wiredi.retry.policy;

import java.util.ArrayList;
import java.util.List;

public abstract class RetryExceptionBarrier {

	protected final List<Class<? extends Throwable>> entries = new ArrayList<>();

	public RetryExceptionBarrier add(Class<? extends Throwable> type) {
		entries.add(type);
		return this;
	}

	public RetryExceptionBarrier addAll(RetryExceptionBarrier other) {
		this.entries.addAll(other.entries);
		return this;
	}

	abstract boolean willRetryFor(Exception e);

}
