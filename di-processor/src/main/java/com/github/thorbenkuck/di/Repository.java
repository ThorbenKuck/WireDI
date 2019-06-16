package com.github.thorbenkuck.di;

import javax.inject.Provider;

public interface Repository {
	boolean isLoaded();

	<T> T getInstance(Class<T> type);

	<T> Provider<T> toProvider(Class<T> type);
}
