package com.github.thorbenkuck.di;

public interface IdentifiableProvider<T> extends Comparable<IdentifiableProvider<?>> {

	Class<?> type();

	Class<?>[] wiredTypes();

	boolean singleton();

	T get(Repository wiredTypes);

	int DEFAULT_PRIORITY = 0;

	default int priority() {
		return DEFAULT_PRIORITY;
	}

	@Override
	default int compareTo(IdentifiableProvider<?> that) {
		return Integer.compare(that.priority(), priority());
	}
}
