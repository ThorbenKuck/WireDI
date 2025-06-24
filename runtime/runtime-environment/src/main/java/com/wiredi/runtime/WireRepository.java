package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.async.StateFull;
import com.wiredi.runtime.beans.Bean;
import com.wiredi.runtime.beans.BeanContainer;
import com.wiredi.runtime.beans.BeanContainerProperties;
import com.wiredi.runtime.domain.Eager;
import com.wiredi.runtime.domain.ScopeRegistry;
import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.lang.OrderedComparator;
import com.wiredi.runtime.domain.WireRepositoryContextCallback;
import com.wiredi.runtime.domain.errors.ExceptionHandler;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.provider.WrappingProvider;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import com.wiredi.runtime.exceptions.BeanNotFoundException;
import com.wiredi.runtime.exceptions.DiInstantiationException;
import com.wiredi.runtime.properties.PropertyLoader;
import com.wiredi.runtime.qualifier.QualifierType;
import com.wiredi.runtime.resources.ResourceLoader;
import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.types.TypeMapper;
import com.wiredi.runtime.values.Value;
import jakarta.inject.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.UndeclaredThrowableException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.wiredi.runtime.lang.Preconditions.is;

/**
 * The WireRepository is the IOC container of WireDI.
 * Its responsibility is to hold and maintain components, which are named "Wires".
 * <p>
 * This class specifies operations on how to interact with the {@link BeanContainer}.
 * <p>
 * Each {@link WireRepository} has a related {@link Environment} assigned during its creation.
 * <p>
 * Apart from the {@link BeanContainer}, each WireRepository also has an {@link OnDemandInjector} instances that can
 * be used to construct components at runtime and without the need to provide {@link IdentifiableProvider}.
 * This injector uses this repository to resolve dependencies.
 * As the {@link OnDemandInjector} requires reflection to operate, it's generally not recommended to use it.
 * Instead, it's suggested to provide everything required as an {@link IdentifiableProvider}.
 * <p>
 * If you require a WireRepository with features such as {@link }
 *
 * @see BeanContainer
 * @see Environment
 * @see OnDemandInjector
 * @see WiredApplication
 */
public class WireRepository {

    @NotNull
    private static final Logging logger = Logging.getInstance(WireRepository.class);
    @NotNull
    private final BeanContainer beanContainer;
    @NotNull
    private final Value<OnDemandInjector> onDemandInjector = Value.lazy(() -> new OnDemandInjector(this));
    @NotNull
    private final ExceptionHandlerContext exceptionHandler = new ExceptionHandlerContext(this);
    @NotNull
    private final StartupDiagnostics startupDiagnostics = new StartupDiagnostics();
    @NotNull
    private final Environment environment;
    @NotNull
    private final ScopeRegistry scopeRegistry = new ScopeRegistry(this);

    public WireRepository(@NotNull Environment environment, @NotNull BeanContainer beanContainer) {
        this.environment = environment;
        this.beanContainer = beanContainer;
    }

    public ScopeRegistry scopeRegistry() {
        return scopeRegistry;
    }

    /**
     * Creates a new fully configured WireRepository using the default properties.
     * <p>
     * This method is recommended to be used in nearly all scenarios you could imagine.
     *
     * @return a new and fully configured WireRepository.
     */
    public static WireRepository open() {
        Environment environment = Environment.build();
        return open(environment, new BeanContainer(new BeanContainerProperties(environment)));
    }

    public static WireRepository open(BeanContainerProperties properties) {
        return open(Environment.build(), new BeanContainer(properties));
    }

    public static WireRepository open(Environment environment) {
        return open(environment, new BeanContainer(new BeanContainerProperties(environment)));
    }

    public static WireRepository open(Environment environment, BeanContainerProperties properties) {
        return open(environment, new BeanContainer(properties));
    }

    public static WireRepository open(Environment environment, BeanContainer beanContainer) {
        WireRepository repository = create(environment, beanContainer);
        repository.load();

        return repository;
    }

    /**
     * Creates a new, not loaded WireRepository.
     *
     * @return a new WireRepository instance
     */
    public static WireRepository create() {
        Environment environment = Environment.build();
        return create(environment, new BeanContainer(new BeanContainerProperties(environment)));
    }

    public static WireRepository create(BeanContainerProperties properties) {
        return create(Environment.build(), new BeanContainer(properties));
    }

    public static WireRepository create(Environment environment) {
        return create(environment, new BeanContainer(new BeanContainerProperties(environment)));
    }

    public static WireRepository create(Environment environment, BeanContainerProperties properties) {
        return create(environment, new BeanContainer(properties));
    }

    public static WireRepository create(Environment environment, BeanContainer beanContainer) {
        return new WireRepository(environment, beanContainer);
    }

    /**
     * Whether this WireRepository is loaded.
     * <p>
     * If this method returns true, the underlying {@link BeanContainer} is loaded and ready to be used.
     * If it returns false, you can load the repository by calling {@link #load()}
     *
     * @return true, if the {@link BeanContainer} is loaded, otherwise false.
     */
    public boolean isLoaded() {
        return beanContainer.isLoaded();
    }

    /**
     * Whether this WireRepository is not loaded.
     * <p>
     * This is the opposite of {@link #isLoaded()}
     *
     * @return true, if the {@link BeanContainer} is not yet loaded, otherwise false.
     */
    public boolean isNotLoaded() {
        return !isLoaded();
    }

    /**
     * The BeanContainer that is associated with this repository.
     * <p>
     * Though possible, it is not recommended to modify the BeanContainer.
     * It will be configured in the {@link #load()} method.
     * <p>
     * To access beans, it is recommended to use the read methods of this repository.
     *
     * @return the BeanContainer instance.
     */
    @NotNull
    public BeanContainer beanContainer() {
        return beanContainer;
    }

    /**
     * The {@link Environment} relating to this WireRepository.
     * <p>
     * If not explicitly defined while constructing this repository, it's a completely standalone environment.
     * <p>
     * The {@link Environment} of a repository is immutable and can't be changed.
     * It must be explicitly defined when constructing the repository.
     * For details about how to set the {@link Environment}, see the create and open functions.
     *
     * @return the environment of this repository
     * @see #create(Environment)
     * @see #create(Environment, BeanContainerProperties)
     * @see #create(Environment, BeanContainer)
     * @see #open(Environment)
     * @see #open(Environment, BeanContainerProperties)
     * @see #open(Environment, BeanContainer)
     */
    public Environment environment() {
        return environment;
    }

    /**
     * Returns the {@link OnDemandInjector} for this WireRepository.
     * <p>
     * The {@link OnDemandInjector} is instantiated lazily as it fulfills a special purpose.
     * It uses reflections to construct a class that is unknown at compile-time.
     * <p>
     * For more details about the {@link OnDemandInjector} please have a look at its documentation.
     *
     * @return the {@link OnDemandInjector} of this WireRepository
     * @see OnDemandInjector
     */
    @NotNull
    public OnDemandInjector onDemandInjector() {
        return onDemandInjector.get();
    }

    /**
     * Returns the {@link ExceptionHandlerContext} linked to this WireRepository.
     * <p>
     * You can manually modify it, but in general this is unnecessary.
     * Provide a Bean of the {@link ExceptionHandler} interface and it will be registered automatically.
     *
     * @return The {@link ExceptionHandlerContext} linked to this WireRepository
     * @see ExceptionHandlerContext
     * @see ExceptionHandler
     */
    public ExceptionHandlerContext exceptionHandler() {
        return exceptionHandler;
    }

    /**
     * Returns the {@link StartupDiagnostics} linked to this WireRepository.
     *
     * @return The {@link StartupDiagnostics} linked to this WireRepository
     */
    public StartupDiagnostics startupDiagnostics() {
        return startupDiagnostics;
    }

    /**
     * Announces a new object to be maintained in this WireRepository.
     * <p>
     * The announced {@code object} will be treated as a singleton instance when referenced, by wrapping it
     * in the {@link IdentifiableProvider#singleton(Object)} instance.
     * <p>
     * For more details about the logic behind this, see the dependent method {@link #announce(IdentifiableProvider)}
     *
     * @param object The instance to maintain
     * @param <T>    The generic of the object.
     * @see #announce(IdentifiableProvider)
     * @see IdentifiableProvider
     * @see IdentifiableProvider#singleton(Object)
     */
    public <T> boolean announce(@NotNull final T object) {
        return announce(IdentifiableProvider.singleton(object));
    }

    /**
     * Announces an {@link IdentifiableProvider} to be maintained in this WireRepository.
     * <p>
     * The provider is respected in any later {@code get} calls.
     * <p>
     * Before the provider is registered, its {@link LoadCondition} is evaluated.
     * If it doesn't match, the method will not register the provider and instead return false.
     * Otherwise, the provider will be passed to the {@link BeanContainer}, where it will be respected from now on.
     * <p>
     * This method exists to allow manual modifications of the WireRepository.
     * One of the use cases for this is the integration of existing IOC containers into WireDI.
     * <p>
     * You can combine this method with the {@link WireRepositoryContextCallback} to register the IdentifiableProvider
     * at the correct point in time.
     *
     * @param identifiableProvider The provider to maintain
     * @param <T>                  The type of the instance in the provider
     * @return true, if the {@link IdentifiableProvider} was successfully registered, otherwise false
     * @see IdentifiableProvider
     * @see LoadCondition
     * @see #announce(Object)
     */
    public <T> boolean announce(@NotNull IdentifiableProvider<T> identifiableProvider) {
        LoadCondition condition = identifiableProvider.condition();
        if (condition != null) {
            ConditionEvaluation.Context context = new ConditionEvaluation.Context(this, identifiableProvider);
            condition.test(context);
            if (!context.isMatched()) {
                return false;
            }
        }

        beanContainer.register(identifiableProvider);
        return true;
    }

    /**
     * Loads the wireRepository, and thereby the underlying {@link BeanContainer}.
     *
     * @return the time it took to load the WireRepository.
     */
    public Timed load() {
        return load((w) -> LoadConfig.DEFAULT);
    }

    /**
     * Loads the wireRepository, and thereby the underlying {@link BeanContainer}.
     * <p>
     * If the {@code loadConfigFunction} function returns true, the repository automatically loads all providers
     * implementing the {@link Eager} interface.
     *
     * @return the time it took to load the WireRepository.
     */
    public Timed load(Function<WireRepository, LoadConfig> loadConfigFunction) {
        is(isNotLoaded(), () -> "The WireRepository is already loaded");
        logger.debug(() -> "Loading WireRepository");
        return startupDiagnostics.measure("WireRepository.load", () -> {
            announce(IdentifiableProvider.singleton(environment, TypeIdentifier.just(Environment.class)));
            announce(IdentifiableProvider.singleton(environment.typeMapper(), TypeIdentifier.just(TypeMapper.class)));
            announce(IdentifiableProvider.singleton(environment.resourceLoader(), TypeIdentifier.just(ResourceLoader.class)));
            announce(IdentifiableProvider.singleton(environment.propertyLoader(), TypeIdentifier.just(PropertyLoader.class)));
            announce(IdentifiableProvider.singleton(startupDiagnostics, TypeIdentifier.just(StartupDiagnostics.class)));

            announce(IdentifiableProvider.singleton(this, TypeIdentifier.just(WireRepository.class)));
            announce(IdentifiableProvider.singleton(beanContainer, TypeIdentifier.just(BeanContainer.class)));
            announce(IdentifiableProvider.singleton(beanContainer.properties(), TypeIdentifier.just(BeanContainerProperties.class)));
            announce(IdentifiableProvider.singleton(exceptionHandler, TypeIdentifier.just(ExceptionHandlerContext.class)));

            startupDiagnostics.measure("BeanContainer.load", () -> beanContainer.load(this));
            LoadConfig loadConfig = loadConfigFunction.apply(this);

            if (loadConfig.initializeEagerBeans) {
                startupDiagnostics.measure("WireRepository.initializeEagerBeans", this::initializeEagerBeans);
            }

            if (loadConfig.synchronizeOnStates) {
                startupDiagnostics.measure("WireRepository.synchronizeOnStates", () -> synchronizeOnStates(loadConfig.stateFullMaxTimeout));
            }
        }).then(time -> logger.debug(() -> "The WireRepository has been loaded in " + time))
                .then(startupDiagnostics::seal);
    }

    private Timed initializeEagerBeans() {
        return Timed.of(() -> {
            logger.trace(() -> "Checking for eager classes");
            final List<Eager> eagerInstances = getAll(Eager.class);
            if (!eagerInstances.isEmpty()) {
                final EagerInitializer initializer = tryGet(EagerInitializer.class).orElse(new EagerInitializer.ParallelStream());
                logger.debug(() -> "Loading " + eagerInstances.size() + " eager classes.");
                initializer.initialize(this, eagerInstances);
            }
        });
    }

    /**
     * Synchronizes on all {@link StateFull} instances in the wire repository.
     * <p>
     * This method waits for all {@link StateFull} instances to have their state set
     * before returning. If a timeout is specified, it will wait for at most that duration.
     *
     * @param timeout the maximum duration to wait, or null to wait indefinitely
     */
    private Timed synchronizeOnStates(@Nullable Duration timeout) {
        return Timed.of(() -> {
            logger.trace(() -> "Synchronizing in states");
            // Writing StateFull<?> right here leads to compile time errors, this
            // is why we explicitly skip the raw type inspection with the following comment
            final List<StateFull<?>> stateFulls = getAll(TypeIdentifier.just(StateFull.class).cast());
            final StateFullInitializer stateFullInitializer = tryGet(StateFullInitializer.class).orElse(new StateFullInitializer.ParallelStream());
            if (!stateFulls.isEmpty()) {
                logger.debug(() -> "Synchronizing on " + stateFulls.size() + " StateFull instances.");
                stateFullInitializer.initialize(this, stateFulls, timeout);
            }
        });
    }

    public record LoadConfig(
            boolean initializeEagerBeans,
            boolean synchronizeOnStates,
            @Nullable Duration stateFullMaxTimeout
    ) {
        public static final LoadConfig DEFAULT = new LoadConfig();

        public LoadConfig() {
            this(true, true, null);
        }

        public LoadConfig initializeEagerBeans(boolean initializeEagerBeans) {
            return new LoadConfig(initializeEagerBeans, synchronizeOnStates, stateFullMaxTimeout);
        }

        public LoadConfig synchronizeOnStates(boolean synchronizeOnStates) {
            return new LoadConfig(initializeEagerBeans, synchronizeOnStates, stateFullMaxTimeout);
        }
    }

    public Timed clear() {
        is(isLoaded(), () -> "The WiredApplication is not loaded");
        logger.debug(() -> "Clearing the WireRepository");
        return Timed.of(() -> {
            beanContainer.clear();
            onDemandInjector.ifPresent(OnDemandInjector::clear);
        }).then(time -> logger.debug(() -> "The WireRepository has been cleared in " + time));
    }

    /**
     * Method to determine if a certain type is maintained iin this repository.
     *
     * @param type the type to search for
     * @return true, if a bean is registered, otherwise false.
     */
    public boolean contains(@NotNull final Class<?> type) {
        return contains(TypeIdentifier.of(type));
    }

    public boolean contains(@NotNull final TypeIdentifier<?> type) {
        return scopeRegistry.getDefaultScope().contains(QualifiedTypeIdentifier.unqualified(type));
//        if (type.isAssignableFrom(IdentifiableProvider.class)) {
//            return beanContainer.get(type.getGenericTypes().getFirst()).isPresent();
//        } else {
//            return !beanContainer.getAll(type).isEmpty();
//        }
    }

    /* ############ Try Get methods ############ */
    public <T> Optional<T> tryGet(@NotNull final Class<T> type) {
        return tryGet(TypeIdentifier.of(type));
    }

    public <T> Optional<T> tryGet(@NotNull final TypeIdentifier<T> type) {
        return beanContainer.get(type).optional(this, type);
    }

    public <T> Optional<T> tryGet(@NotNull final Class<T> type, QualifierType qualifierType) {
        return tryGet(TypeIdentifier.just(type), qualifierType);
    }

    public <T> Optional<T> tryGet(@NotNull final TypeIdentifier<T> type, QualifierType qualifierType) {
        return beanContainer.get(type, qualifierType).optional(this, type);
    }

    /* ############ Get methods ############ */
    public <T> T get(@NotNull final Class<T> type) {
        return get(TypeIdentifier.of(type));
    }

    public <T> T get(@NotNull final TypeIdentifier<T> type) {
        return safeRun(() -> beanContainer.get(type).instantiate(this, type));
    }

    public <T> T get(@NotNull final Class<T> type, QualifierType qualifierType) {
        return get(TypeIdentifier.of(type), qualifierType);
    }

    public <T> T get(@NotNull final TypeIdentifier<T> type, QualifierType qualifierType) {
        return safeRun(() -> tryGet(type, qualifierType).orElseThrow(() -> new BeanNotFoundException(type, qualifierType, this)));
    }

    /* ############ Get all methods ############ */
    public <T> List<T> getAll(Class<T> type) {
        return getAll(TypeIdentifier.of(type));
    }

    public <T> List<T> getAll(TypeIdentifier<T> type) {
        return safeRun(() -> beanContainer.getAll(type)
                .stream()
                .sorted(OrderedComparator.INSTANCE)
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

    /* ############ Get ObjectReference methods ############ */
    public <T> ObjectReference<T> getReference(@NotNull final Class<T> type) {
        return getReference(TypeIdentifier.of(type));
    }

    public <T> ObjectReference<T> getReference(@NotNull final TypeIdentifier<T> type) {
        return new ObjectReference<>(this, type);
    }

    /* ############ Get Bean methods ############ */
    public <T> Bean<T> getBean(TypeIdentifier<T> typeIdentifier) {
        return beanContainer.access(typeIdentifier);
    }

    public <T> Bean<T> getBean(Class<T> typeIdentifier) {
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
                exceptionHandler.handle(throwable);
                throw throwable;
            } catch (final Throwable e) {
                if (RuntimeException.class.isAssignableFrom(e.getClass())) {
                    throw (RuntimeException) e;
                }
                throw new UndeclaredThrowableException(e);
            }
        }
    }

    @Nullable
    private <T> T instantiate(
            @NotNull final IdentifiableProvider<T> provider,
            @NotNull final TypeIdentifier<T> type
    ) {
        try {
            return provider.get(this, type);
        } catch (final Exception e) {
            throw new DiInstantiationException("Error while wiring " + provider.type(), type, e);
        }
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
}
