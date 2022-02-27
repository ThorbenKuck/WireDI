package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.domain.WireCapable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;

public abstract class SynchronizedServiceLoader<T extends WireCapable> {

	@NotNull
	protected final DataAccess dataAccess = new DataAccess();

	protected volatile boolean loaded = false;

	@NotNull
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

	@NotNull
	public abstract Class<T> serviceType();

	public final void register(@NotNull final T t) {
		dataAccess.write(() -> {
			for (final Class<?> wiredType : t.wiredTypes()) {
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

	protected void unsafeRegister(@NotNull final Class<?> type, @NotNull final T instance) {
		final List<T> providers = mapping.computeIfAbsent(type, (t) -> new ArrayList<>());
		providers.add(instance);
	}

	@NotNull
	protected Map<Class<?>, List<T>> getAll() {
		return mapping;
	}

	@NotNull
	protected List<T> unsafeGet(@NotNull final Class<?> type) {
		return mapping.getOrDefault(type, Collections.emptyList());
	}

	@Override
	@NotNull
	public final String toString() {
		return getClass().getSimpleName() + "{" +
				"registrations=" + getAll() +
				", loaded=" + loaded +
				'}';
	}
}
