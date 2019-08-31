package com.github.thorbenkuck.di;

import java.util.ServiceLoader;

public abstract class SynchronizedServiceLoader<T> {

	private final Object loadLock = new Object();
	protected volatile boolean loaded;

	public void load() {
		// pre check to avoid unnecessary synchronization
		if (loaded) {
			return;
		}
		synchronized (loadLock) {
			// Check again, to combat race conditions,
			// where both threads pass the pre checks
			// and then, one after another enter this
			// synchronized statement and then override
			// the same instances.
			// We want to ensure under any and all
			// circumstance, that we only load once.
			if (loaded) {
				return;
			}

			ServiceLoader.load(serviceType())
					.forEach(this::add);
			loaded = true;
		}
	}

	public abstract Class<T> serviceType();

	public abstract void add(T t);

	public final boolean isLoaded() {
		return loaded;
	}

}
