package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.domain.WireCapable;

import java.util.*;

public abstract class SynchronizedServiceLoader<T extends WireCapable> {

	protected final DataAccess dataAccess = new DataAccess();
	protected volatile boolean loaded;
	private final Map<Class<?>, List<T>> mapping = new HashMap<>();

	public void unload() {
		mapping.clear();
		loaded = false;
	}

	public void load() {
		// pre check to avoid unnecessary synchronization
		if (loaded) {
			return;
		}
		dataAccess.write(() -> {
			// Check again, to combat race conditions,
			// where both threads pass the pre-checks
			// and then, one after another enter this
			// synchronized statement and then override
			// the same instances.
			// We want to ensure under any and all
			// circumstance, that we only load once.
			if (loaded) {
				return;
			}

			ServiceLoader.load(serviceType())
					.forEach(this::register);
			loaded = true;
		});
	}

	public abstract Class<T> serviceType();

	public final void register(T t) {
		dataAccess.write(() -> {
			for (Class<?> wiredType : t.wiredTypes()) {
				if (wiredType == null) {
					throw new DiLoadingException("The WireCables " + t + " returned null as an identifiable type! This is not permitted.\n" +
							"If you did not create your own instance, please submit your annotated class to github.");
				}
				unsafeRegister(wiredType, t);
			}
		});
	}

	public final boolean isLoaded() {
		return loaded;
	}

	protected void unsafeRegister(Class<?> type, T instance) {
		List<T> providers = mapping.computeIfAbsent(type, (t) -> new ArrayList<>());
		providers.add(instance);
	}

	protected List<T> unsafeGet(Class<?> type) {
		return mapping.getOrDefault(type, Collections.emptyList());
	}
}
