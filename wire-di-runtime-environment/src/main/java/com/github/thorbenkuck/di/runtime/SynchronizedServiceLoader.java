package com.github.thorbenkuck.di.runtime;

import com.github.thorbenkuck.di.domain.WireCapable;
import com.github.thorbenkuck.di.lang.DataAccess;
import com.github.thorbenkuck.di.runtime.exceptions.DiLoadingException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class SynchronizedServiceLoader<T extends WireCapable> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@NotNull
	protected final DataAccess dataAccess = new DataAccess();

	protected volatile boolean loaded = false;

	@NotNull
	private final Map<Class<?>, List<T>> mapping = new HashMap<>();

	public void unload() {
		dataAccess.write(() -> {
			logger.info("Clearing cached mappings");
			mapping.clear();
			loaded = false;
		});
	}

	public Timed load() {
		// pre check to avoid unnecessary synchronization
		if (loaded) {
			return Timed.empty();
		}
		return dataAccess.write(() -> {
			// Check again, to combat race conditions,
			// where both threads pass the pre-checks
			// and then, one after another enter this
			// synchronized statement and then override
			// the same instances.
			// We want to ensure under any and all
			// circumstance, that we only load once.
			if (loaded) {
				return Timed.empty();
			}

			logger.debug("Starting to load {}", serviceType());
			Timed timed = Timed.of(() -> {
				ServiceLoader.load(serviceType())
						.forEach(this::register);
				ServiceLoader.loadInstalled(serviceType())
						.forEach(this::register);

				loaded = true;
			});

			logger.info("Loading finished in {}ms", timed.get(TimeUnit.MILLISECONDS));
			return timed;
		});
	}

	@NotNull
	protected abstract Class<T> serviceType();

	public final void register(@NotNull final T t) {
		dataAccess.write(() -> {
			logger.debug("Registering instance of type {} with wired types {}", t.getClass(), Arrays.toString(t.wiredTypes()));
			for (final Class<?> wiredType : t.wiredTypes()) {
				if (wiredType == null) {
					throw new DiLoadingException("The WireCables " + t + " returned null as an identifiable type! This is not permitted.\n" +
							"If you did not create your own instance, please submit your annotated class to github.");
				}
				logger.trace("Registering {} for {}", t, wiredType);
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
