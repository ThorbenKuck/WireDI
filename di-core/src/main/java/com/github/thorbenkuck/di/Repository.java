package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.resources.Resources;

import javax.inject.Provider;

public interface Repository {
	boolean isLoaded();

	Resources getResourceRepository();

	<T> T getInstance(Class<T> type);

	<T> Provider<T> getProvider(Class<T> type);
}
