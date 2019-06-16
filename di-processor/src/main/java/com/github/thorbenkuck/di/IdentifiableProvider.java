package com.github.thorbenkuck.di;

public interface IdentifiableProvider<T> {

	Class type();

	Class[] wiredTypes();

	boolean lazy();

	T get();

	default void instantiate(Repository wiredTypes) {
	}

}
