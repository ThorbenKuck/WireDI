package com.wiredi.runtime.beans;

import com.wiredi.domain.WireConflictResolver;
import com.wiredi.domain.WireConflictStrategy;
import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.domain.provider.TypeIdentifier;
import com.wiredi.lang.DataAccess;
import com.wiredi.lang.time.Timed;
import com.wiredi.qualifier.QualifierType;
import com.wiredi.runtime.Loader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BeanContainer {

	private final Logger logger = LoggerFactory.getLogger(BeanContainer.class);
	@NotNull
	private final DataAccess dataAccess = new DataAccess();
	@NotNull
	private final Map<TypeIdentifier<?>, ModifiableBean<?>> mapping = new HashMap<>();
	private volatile boolean loaded = false;
	private WireConflictResolver wireConflictResolver = WireConflictStrategy.DIRECT_MATCH;

	public void clear() {
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
		return dataAccess.writeValue(() -> {
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

			AtomicInteger count = new AtomicInteger();
			logger.debug("Registering all known identifiable providers");
			Timed timed = Timed.of(() -> {
				List<IdentifiableProvider> identifiableProviders = Loader.identifiableProviders();
				registerAll(identifiableProviders);
				count.set(identifiableProviders.size());
				loaded = true;
			});
			logger.info("Registered {} identifiable providers in {}", count.get(), timed);

			return timed;
		});
	}

	public final <T> void register(@NotNull final IdentifiableProvider<T> t) {
		dataAccess.write(() -> unsafeRegister(t));
	}

	public final void registerAll(@NotNull final List<@NotNull IdentifiableProvider> list) {
		dataAccess.write(() -> list.forEach(this::unsafeRegister));
	}

	private <T> void unsafeRegister(@NotNull final IdentifiableProvider<T> t) {
		logger.trace("Registering instance of type {} with wired types {} and qualifiers {}", t.getClass(), t.additionalWireTypes(), t.qualifiers());
		for (final TypeIdentifier<?> wiredType : t.additionalWireTypes()) {
			logger.trace("Registering {} for {}", t, wiredType);
			unsafeGetOrCreate(wiredType).put((IdentifiableProvider) t);
		}
		unsafeGetOrCreate(t.type()).put(t);
	}

	public final boolean isLoaded() {
		return loaded;
	}

	public <T> ModifiableBean<T> access(TypeIdentifier<T> typeIdentifier) {
		return Optional.ofNullable(
				dataAccess.readValue(() -> unsafeGet(typeIdentifier))
		).orElse(ModifiableBean.empty());
	}

	public <T> ModifiableBean<T> accessOrCreate(TypeIdentifier<T> typeIdentifier) {
		return Optional.ofNullable(
				dataAccess.readValue(() -> unsafeGet(typeIdentifier))
		).orElseGet(() -> dataAccess.writeValue(() -> unsafeGetOrCreate(typeIdentifier)));
	}

	public <T> Optional<IdentifiableProvider<T>> get(final TypeIdentifier<T> typeIdentifier) {
		return Optional.ofNullable(
				dataAccess.readValue(() -> unsafeGet(typeIdentifier))
		).flatMap(it -> it.get(wireConflictResolver));
	}

	public <T> Optional<IdentifiableProvider<T>> get(final TypeIdentifier<T> typeIdentifier, QualifierType qualifierType) {
		return Optional.ofNullable(
				dataAccess.readValue(() -> unsafeGet(typeIdentifier))
		).flatMap(it -> it.get(qualifierType));
	}

	public <T> List<IdentifiableProvider<T>> getAll(final TypeIdentifier<T> typeIdentifier) {
		return Optional.ofNullable(
				dataAccess.readValue(() -> unsafeGet(typeIdentifier))
		).map(ModifiableBean::getAll).orElse(Collections.emptyList());
	}

	@Override
	@NotNull
	public final String toString() {
		return getClass().getSimpleName() + "{" +
				"loaded=" + loaded +
				'}';
	}

	private <T> ModifiableBean<T> unsafeGet(@NotNull final TypeIdentifier<T> type) {
		return (ModifiableBean<T>) mapping.get(type);
	}

	private <T> ModifiableBean<T> unsafeGetOrCreate(@NotNull final TypeIdentifier<T> type) {
		return (ModifiableBean<T>) mapping.computeIfAbsent(type, t -> new ModifiableBean<>(type));
	}
}
