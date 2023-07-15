package com.wiredi.runtime;

import com.wiredi.domain.*;
import com.wiredi.domain.errors.ErrorHandler;
import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.domain.provider.TypeIdentifier;
import com.wiredi.domain.provider.WrappingProvider;
import com.wiredi.environment.Environment;
import com.wiredi.lang.StateFull;
import com.wiredi.lang.time.Timed;
import com.wiredi.properties.PropertyLoader;
import com.wiredi.properties.TypedProperties;
import com.wiredi.qualifier.QualifierType;
import com.wiredi.resources.ResourceLoader;
import com.wiredi.runtime.banner.Banner;
import com.wiredi.runtime.beans.BeanContainer;
import com.wiredi.runtime.beans.ModifiableBean;
import com.wiredi.runtime.exceptions.BeanNotFoundException;
import com.wiredi.runtime.exceptions.DiInstantiationException;
import jakarta.inject.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.wiredi.lang.Preconditions.require;

public class WireRepository {

	@NotNull
	private static final Logger logger = LoggerFactory.getLogger(WireRepository.class);

	@NotNull
	private final WiredTypesProperties properties = new WiredTypesProperties();

	@NotNull
	private final Environment environment = new Environment();

	@NotNull
	private final BeanContainer beanContainer = new BeanContainer();

	@NotNull
	private final Banner banner = new Banner();

	protected WireRepository() {
	}

	public static WireRepository open() {
		WireRepository wiredTypes = new WireRepository();
		wiredTypes.load();

		return wiredTypes;
	}

	public static WireRepository create() {
		return new WireRepository();
	}

	private void registerFixedInstances() {
		Timed.of(() -> {
			logger.trace("Registering all known static types");
			announce(IdentifiableProvider.singleton(properties, TypeIdentifier.of(WiredTypesProperties.class)));
			announce(IdentifiableProvider.singleton(beanContainer, TypeIdentifier.of(BeanContainer.class)));
			announce(IdentifiableProvider.singleton(this, TypeIdentifier.of(WireRepository.class)));
			announce(IdentifiableProvider.singleton(banner, TypeIdentifier.of(Banner.class)));
			announce(IdentifiableProvider.singleton(environment, TypeIdentifier.of(Environment.class)));
			announce(IdentifiableProvider.singleton(environment.resourceLoader(), TypeIdentifier.of(ResourceLoader.class)));
			announce(IdentifiableProvider.singleton(environment.properties(), TypeIdentifier.of(TypedProperties.class)));
			announce(IdentifiableProvider.singleton(environment.propertyLoader(), TypeIdentifier.of(PropertyLoader.class)));
		}).then(time -> logger.trace("Registered known types in {}", time));
	}

	public Banner banner() {
		return banner;
	}

	public boolean isLoaded() {
		return beanContainer.isLoaded();
	}

	public boolean isNotLoaded() {
		return !isLoaded();
	}

	@NotNull
	public Environment environment() {
		return this.environment;
	}

	@NotNull
	public WiredTypesProperties properties() {
		return properties;
	}

	public <T> void announce(@NotNull final T o) {
		announce(IdentifiableProvider.singleton(o));
	}

	public <T> void announce(@NotNull IdentifiableProvider<T> identifiableProvider) {
		beanContainer.register(identifiableProvider);
	}

	private List<WireRepositoryContextCallbacks> getCallbacks() {
		if (properties.contextCallbacksEnabled()) {
			return Loader.contextCallbacks();
		} else {
			return Collections.singletonList(new LoggingWireRepositoryContextCallbacks());
		}
	}

	@NotNull
	public Timed load() {
		require(isNotLoaded(), () -> "The WireRepository is already loaded");
		List<WireRepositoryContextCallbacks> contextCallbacks = getCallbacks();

		return safeTimed(() -> {
			banner.print();
			logger.debug("Loading WireRepository");
			contextCallbacks.forEach(it -> it.loadingStarted(this));

			Timed.of(() -> {
				logger.debug("Loading Environment");
				environment.autoconfigure();
				properties.load(environment);
			}).then(timed -> contextCallbacks.forEach(it -> it.loadedEnvironment(timed, environment)));

			Timed.of(() -> {
				logger.debug("Loading BeanContainer");
				registerFixedInstances();
				beanContainer.load();
			}).then(timed -> contextCallbacks.forEach(callback -> callback.loadedBeanContainer(timed, beanContainer)));

			if (properties.loadEagerInstance()) {
				loadEagerClasses(contextCallbacks);
			}

			if (properties.awaitStates()) {
				synchronizeOnStates();
			}
		}).then(totalTime -> contextCallbacks.forEach(callback -> callback.loadingFinished(totalTime, this)));
	}

	private void loadEagerClasses(List<WireRepositoryContextCallbacks> contextCallbacks) {
		logger.debug("Checking for eager classes");
		final List<Eager> eagerInstances = getAll(Eager.class);
		if (!eagerInstances.isEmpty()) {
			Timed.of(() -> eagerInstances.parallelStream().forEach(it -> it.setup(this)))
					.then(timed -> contextCallbacks.forEach(callback -> callback.loadedEagerClasses(timed, eagerInstances)));
		}
	}

	public void synchronizeOnStates() {
		logger.debug("Synchronizing on states");
		final List<StateFull> stateFulls = getAll(TypeIdentifier.just(StateFull.class));
		if (!stateFulls.isEmpty()) {
			Timed.of(() -> stateFulls.parallelStream()
							.map(StateFull::getState)
							.forEach(state -> state.awaitUntilSet(properties.awaitStatesTimeout())))
					.then(timed -> logger.info("All states have loaded in {}", timed));
		}
	}

	/* ############ Try Get methods ############ */
	public <T> Optional<T> tryGet(@NotNull final Class<T> type) {
		return tryGet(TypeIdentifier.of(type));
	}

	public <T> Optional<T> tryGet(@NotNull final TypeIdentifier<T> type) {
		return beanContainer.get(type).map(it -> instantiate(it, type));
	}

	public <T> Optional<T> tryGet(@NotNull final TypeIdentifier<T> type, QualifierType qualifierType) {
		return beanContainer.get(type, qualifierType).map(it -> instantiate(it, type));
	}

	/* ############ Get methods ############ */
	public <T> T get(@NotNull final Class<T> type) {
		return get(TypeIdentifier.of(type));
	}


	public <T> T get(@NotNull final TypeIdentifier<T> type) {
		return safeRun(() -> tryGet(type).orElseThrow(() -> new BeanNotFoundException(type)));
	}


	public <T> T get(@NotNull final TypeIdentifier<T> type, QualifierType qualifierType) {
		return safeRun(() -> tryGet(type, qualifierType).orElseThrow(() -> new BeanNotFoundException(type, qualifierType)));
	}

	/* ############ Get all methods ############ */
	public <T> List<T> getAll(Class<T> type) {
		return getAll(TypeIdentifier.of(type));
	}

	public <T> List<T> getAll(TypeIdentifier<T> type) {
		return safeRun(() -> beanContainer.getAll(type)
				.stream()
				.sorted(OrderComparator.INSTANCE)
				.map(it -> instantiate(it, type))
				.filter(Objects::nonNull)
				.toList()
		);
	}

	public <T> List<T> getAllUnordered(TypeIdentifier<T> type) {
		return safeRun(() -> beanContainer.getAll(type)
				.stream()
				.map(it -> instantiate(it, type))
				.filter(Objects::nonNull)
				.toList()
		);
	}

	/* ############ Get provider methods ############ */
	@NotNull
	public <T> Provider<T> getProvider(@NotNull final Class<T> type) {
		return getProvider(TypeIdentifier.of(type));
	}

	public <T> Provider<T> getProvider(@NotNull final TypeIdentifier<T> type) {
		final IdentifiableProvider<T> provider = requireSingleProvider(type);
		return toProvider(provider);
	}

	public <T> Provider<T> getProvider(TypeIdentifier<T> typeIdentifier, QualifierType qualifierType) {
		final IdentifiableProvider<T> provider = requireSingleProvider(typeIdentifier, qualifierType);
		return toProvider(provider);
	}

	/* ############ Get Native Provider methods ############ */
	public <T> IdentifiableProvider<T> getNativeProvider(Class<T> typeIdentifier) {
		return getNativeProvider(TypeIdentifier.of(typeIdentifier));
	}

	public <T> IdentifiableProvider<T> getNativeProvider(TypeIdentifier<T> typeIdentifier) {
		return requireSingleProvider(typeIdentifier);
	}

	public <T> IdentifiableProvider<T> getNativeProvider(TypeIdentifier<T> typeIdentifier, QualifierType qualifierType) {
		return requireSingleProvider(typeIdentifier, qualifierType);
	}

	/* ############ Get Bean methods ############ */
	public <T> ModifiableBean<T> getBean(TypeIdentifier<T> typeIdentifier) {
		return beanContainer.access(typeIdentifier);
	}


	public <T> ModifiableBean<T> getBean(Class<T> typeIdentifier) {
		return getBean(TypeIdentifier.of(typeIdentifier));
	}

	@NotNull
	public <T> Provider<T> toProvider(@NotNull final IdentifiableProvider<T> identifiableProvider) {
		return new WrappingProvider<>(identifiableProvider, this);
	}

	@NotNull
	public <T> T safeRun(@NotNull Supplier<T> supplier) {
		try {
			return supplier.get();
		} catch (final Throwable throwable) {
			try {
				handleError(throwable);
				throw throwable;
			} catch (final Throwable e) {
				if (RuntimeException.class.isAssignableFrom(e.getClass())) {
					throw (RuntimeException) e;
				}
				throw new UndeclaredThrowableException(e);
			}
		}
	}

	@NotNull
	public Timed safeTimed(@NotNull Runnable runnable) {
		return Timed.of(() -> safeRun(runnable));
	}

	public void safeRun(@NotNull Runnable runnable) {
		try {
			runnable.run();
		} catch (final Throwable throwable) {
			try {
				handleError(throwable);
			} catch (final Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}

	public <T extends Throwable> void handleError(@NotNull final T throwable) throws Throwable {
		List<ErrorHandler<T>> errorHandlers = getAll(
				TypeIdentifier.of(ErrorHandler.class)
						.withGeneric(throwable.getClass())
		);
		if (errorHandlers.isEmpty()) {
			throw throwable;
		}
		for (ErrorHandler<T> errorHandler : errorHandlers) {
			var handlingResult = errorHandler.handle(throwable);
			if (handlingResult.valid()) {
				handlingResult.print();
				if (handlingResult.unrecoverable()) {
					handlingResult.doThrow();
				}
				Supplier<Throwable> rethrow = handlingResult.rethrow();
				if (rethrow != null) {
					throw rethrow.get();
				}
				return;
			}
		}
	}

	@Nullable
	private <T> T instantiate(
			@NotNull final IdentifiableProvider<T> provider,
			@NotNull final TypeIdentifier<T> type
	) {
		try {
			return provider.get(this);
		} catch (final Exception e) {
			throw wireCreationError(e, type, provider);
		}
	}

	@NotNull
	private <T> T instantiateNotNull(
			@NotNull final IdentifiableProvider<T> provider,
			@NotNull TypeIdentifier<T> type
	) {
		T instance = instantiate(provider, type);
		if (instance == null) {
			throw new DiInstantiationException("Provider produced null for type " + type + ". This is not allowed by design!", type);
		}
		return instance;
	}

	@NotNull
	private DiInstantiationException wireCreationError(
			@NotNull final Exception e,
			@NotNull final TypeIdentifier<?> wireType,
			@NotNull final IdentifiableProvider<?> provider
	) {
		return new DiInstantiationException("Error while wiring " + provider.type(), wireType, e);
	}

	@NotNull
	private <T> IdentifiableProvider<T> requireSingleProvider(@NotNull final TypeIdentifier<T> type) {
		return beanContainer.get(type)
				.orElseThrow(() -> new DiInstantiationException("Could not find any instance for " + type, type));
	}

	@NotNull
	private <T> IdentifiableProvider<T> requireSingleProvider(@NotNull final TypeIdentifier<T> type, QualifierType qualifierType) {
		return beanContainer.get(type, qualifierType)
				.orElseThrow(() -> new DiInstantiationException("Could not find any instance for " + type, type));
	}

	@NotNull
	private <T> IdentifiableProvider<T> findPrimaryProvider(
			@NotNull final List<IdentifiableProvider> providers,
			@NotNull final TypeIdentifier<T> type
	) {
		if (providers.isEmpty()) {
			throw new DiInstantiationException("No provider registered for type " + type, type);
		}
		if (providers.size() == 1) {
			return (IdentifiableProvider<T>) providers.get(0);
		}
		final WireConflictResolver wireConflictStrategy = properties.conflictStrategy();
		List<IdentifiableProvider<T>> generified = providers.stream()
				.map(it -> (IdentifiableProvider<T>) it)
				.collect(Collectors.toList());
		return wireConflictStrategy.find(generified, type);
	}
}
