package com.wiredi.runtime.beans;

import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.domain.provider.TypeIdentifier;
import com.wiredi.domain.provider.condition.LoadCondition;
import com.wiredi.lang.DataAccess;
import com.wiredi.lang.time.Timed;
import com.wiredi.qualifier.QualifierType;
import com.wiredi.runtime.Loader;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.WiredRepositoryProperties;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.wiredi.lang.Preconditions.require;

public class BeanContainer {

	private final Logger logger = LoggerFactory.getLogger(BeanContainer.class);
	@NotNull
	private final DataAccess dataAccess = new DataAccess();
	@NotNull
	private final Map<TypeIdentifier<?>, ModifiableBean<?>> mapping = new HashMap<>();
	private volatile boolean loaded = false;
	private final WiredRepositoryProperties properties;

	public BeanContainer(@NotNull WiredRepositoryProperties properties) {
		this.properties = properties;
	}

	public void clear() {
		dataAccess.write(() -> {
			logger.info("Clearing cached mappings");
			mapping.clear();
			loaded = false;
		});
	}

	public Timed load(WireRepository wireRepository) {
		// pre check to avoid unnecessary synchronization
		if (loaded) {
			return Timed.ZERO;
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
				return Timed.ZERO;
			}

			AtomicInteger count = new AtomicInteger();
			logger.debug("Registering all known identifiable providers");
			Timed timed = Timed.of(() -> {
				Loader.identifiableProviders().forEach(provider -> {
					LoadCondition condition = provider.condition();
					if (condition != null) {
						if (condition.matches(wireRepository)) {
							count.incrementAndGet();
							unsafeRegister(provider);
						}
					} else {
						count.incrementAndGet();
						unsafeRegister(provider);
					}
				});
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
			unsafeGetOrCreate(wiredType).register((TypeIdentifier) wiredType, (IdentifiableProvider) t);
		}
		unsafeGetOrCreate(t.type()).register((TypeIdentifier) t.type(), (IdentifiableProvider) t);
	}

	public final boolean isLoaded() {
		return loaded;
	}

	public <T> Bean<T> access(TypeIdentifier<T> typeIdentifier) {
		require(typeIdentifier.referenceConcreteType(), () -> "Cannot call access on a reference type (Bean, IdentifiableProvider): " + typeIdentifier);

		return Optional.ofNullable(
				dataAccess.readValue(() -> unsafeGet(typeIdentifier))
		).orElse(ModifiableBean.empty());
	}

	public <T> Bean<T> accessOrCreate(TypeIdentifier<T> typeIdentifier) {
		require(typeIdentifier.referenceConcreteType(), () -> "Cannot call accessOrCreate on a reference type (Bean, IdentifiableProvider): " + typeIdentifier);

		return Optional.ofNullable(
				dataAccess.readValue(() -> unsafeGet(typeIdentifier))
		).orElseGet(() -> dataAccess.writeValue(() -> (ModifiableBean<T>) unsafeGetOrCreate(typeIdentifier)));
	}

	public <T> Optional<IdentifiableProvider<T>> get(final TypeIdentifier<T> concreteType) {
		require(concreteType.referenceConcreteType(), () -> "Cannot call get on a reference type (Bean, IdentifiableProvider): " + concreteType);

		return Optional.ofNullable(
				dataAccess.readValue(() -> unsafeGet(concreteType))
		).flatMap(it -> it.get(concreteType, properties.conflictResolverSupplier()));
	}

	public <T> Optional<IdentifiableProvider<T>> get(final TypeIdentifier<T> typeIdentifier, QualifierType qualifierType) {
		require(typeIdentifier.referenceConcreteType(), () -> "Cannot call get on a reference type (Bean, IdentifiableProvider): " + typeIdentifier);

		return Optional.ofNullable(
				dataAccess.readValue(() -> unsafeGet(typeIdentifier))
		).flatMap(it -> it.get(qualifierType));
	}

	public <T> List<IdentifiableProvider<T>> getAll(final TypeIdentifier<T> typeIdentifier) {
		require(typeIdentifier.referenceConcreteType(), () -> "Cannot call getAll on a reference type (Bean, IdentifiableProvider): " + typeIdentifier);

		return Optional.ofNullable(
				dataAccess.readValue(() -> unsafeGet(typeIdentifier))
		).map(Bean::getAll).orElse(Collections.emptyList());
	}

	private <T> ModifiableBean<T> unsafeGet(@NotNull final TypeIdentifier<T> type) {
		return (ModifiableBean<T>) mapping.get(type.erasure());
	}

	private ModifiableBean<?> unsafeGetOrCreate(@NotNull final TypeIdentifier<?> type) {
		return mapping.computeIfAbsent(type.erasure(), t -> new ModifiableBean<>(type));
	}

	@Override
	@NotNull
	public final String toString() {
		return getClass().getSimpleName() + "{" +
				"loaded=" + loaded +
				'}';
	}
}
