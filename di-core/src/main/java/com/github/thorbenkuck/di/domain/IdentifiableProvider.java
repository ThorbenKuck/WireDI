package com.github.thorbenkuck.di.domain;

public interface IdentifiableProvider<T> extends Comparable<IdentifiableProvider<?>>, WireCapable {

	Class<?> type();

	boolean isSingleton();

	T get(WireRepository wiredTypes);

	int DEFAULT_PRIORITY = 0;

	default int priority() {
		return DEFAULT_PRIORITY;
	}

	@Override
	default int compareTo(IdentifiableProvider<?> that) {
		return Integer.compare(that.priority(), priority());
	}
}
