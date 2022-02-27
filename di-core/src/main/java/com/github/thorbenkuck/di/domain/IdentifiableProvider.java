package com.github.thorbenkuck.di.domain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IdentifiableProvider<T> extends Comparable<IdentifiableProvider<?>>, WireCapable {

	@NotNull
	Class<?> type();

	boolean isSingleton();

	@Nullable
	T get(@NotNull final WireRepository wiredTypes);

	int DEFAULT_PRIORITY = 0;

	default int priority() {
		return DEFAULT_PRIORITY;
	}

	@Override
	default int compareTo(@NotNull final IdentifiableProvider<?> that) {
		return Integer.compare(that.priority(), priority());
	}
}
