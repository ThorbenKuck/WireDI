package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.async.StateFull;
import com.wiredi.runtime.domain.Eager;
import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.ScopeRegistry;
import com.wiredi.runtime.domain.WireContainerCallback;
import com.wiredi.runtime.domain.conditional.ConditionEvaluation;
import com.wiredi.runtime.domain.errors.ExceptionHandler;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.provider.WrappingProvider;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.wiredi.runtime.lang.Preconditions.is;

/**
 * The WireContainer is the IOC container of WireDI.
 * Its responsibility is to hold and maintain components, which are named "Wires".
 * <p>
 * This class specifies operations on how to interact with the {@link WireContainerInitializer}.
 * <p>
 * Each {@link WireContainer} has a related {@link Environment} assigned during its creation.
 * <p>
 * Apart from the {@link WireContainerInitializer}, each WireContainer also has an {@link OnDemandInjector} instances that can
 * be used to construct components at runtime and without the need to provide {@link IdentifiableProvider}.
 * This injector uses this repository to resolve dependencies.
 * As the {@link OnDemandInjector} requires reflection to operate, it's generally not recommended to use it.
 * Instead, it's suggested to provide everything required as an {@link IdentifiableProvider}.
 * <p>
 * If you require a WireContainer with features such as {@link }
 *
 * @see WireContainerInitializer
 * @see Environment
 * @see OnDemandInjector
 * @see WiredApplication
 */
public class WireContainer {

    @NotNull
    private static final Logging logger = Logging.getInstance(WireContainer.class);
    @NotNull
    private final Value<OnDemandInjector> onDemandInjector = Value.lazy(() -> new OnDemandInjector(this));
    @NotNull
    private final Environment environment;
    @NotNull
    private final ExceptionHandlerContext exceptionHandler = new ExceptionHandlerContext(this);
    @NotNull
    private final StartupDiagnostics startupDiagnostics;
    @NotNull
    private final ScopeRegistry scopeRegistry;
    @NotNull
    private final WireContainerInitializer initializer;

    /**
     * Creates a new WireContext with the specified environment.
     * This constructor uses default instances for all other components.
     *
     * @param environment the environment to use
     */
    public WireContainer(@NotNull Environment environment) {
        this.environment = environment;
        this.startupDiagnostics = new StartupDiagnostics();
        this.scopeRegistry = new ScopeRegistry();
        this.initializer = WireContainerInitializer.preconfigured();
        this.exceptionHandler.setWireContext(this);
    }

    /**
     * Creates a new WireContext with the specified components.
     * <p>
     * This constructor allows full customization of the WireContext.
     * It is package private and used by the {@link WireContainerBuilder}
     *
     * @param environment        the environment to use
     * @param startupDiagnostics the startup diagnostics to use, or null to create a default one
     * @param scopeRegistry      the scope registry to use, or null to create a default one
     * @param initializer        the container initializer to use, or null to create a default one
     */
    WireContainer(
            @NotNull Environment environment,
            @NotNull StartupDiagnostics startupDiagnostics,
            @NotNull ScopeRegistry scopeRegistry,
            @NotNull WireContainerInitializer initializer
    ) {
        this.environment = environment;
        this.exceptionHandler.setWireContext(this);
        this.startupDiagnostics = startupDiagnostics;
        this.scopeRegistry = scopeRegistry;
        this.initializer = initializer;
    }

    /**
     * Creates a new fully configured WireContainer using the default properties and loads it.
     * <p>
     * This method is recommended to be used in nearly all scenarios you could imagine.
     * <p>
     * This method uses the {@link WireContainerBuilder} internally.
     *
     * @return a new and fully configured WireContainer.
     * @see WireContainerBuilder
     */
    @NotNull
    public static WireContainer open() {
        return builder().load();
    }

    /**
     * Creates a new fully configured WireContainer using the provided environment and loads it.
     * <p>
     * This method uses the {@link WireContainerBuilder} internally.
     *
     * @param environment the environment to use
     * @return a new and fully configured WireContainer.
     * @see WireContainerBuilder
     */
    @NotNull
    public static WireContainer open(Environment environment) {
        return builder().withEnvironment(environment).load();
    }

    /**
     * Creates a new, not loaded WireContainer.
     * <p>
     * This method uses the {@link WireContainerBuilder} internally.
     *
     * @return a new WireContainer instance
     * @see WireContainerBuilder
     */
    @NotNull
    public static WireContainer create() {
        return builder().build();
    }

    /**
     * Creates a new, not loaded WireContainer with the provided environment.
     * <p>
     * This method uses the {@link WireContainerBuilder} internally.
     *
     * @param environment the environment to use
     * @return a new WireContainer instance
     * @see WireContainerBuilder
     */
    @NotNull
    public static WireContainer create(Environment environment) {
        return builder().withEnvironment(environment).build();
    }

    /**
     * Returns a new {@link WireContainerBuilder} for creating and configuring a WireContext.
     *
     * @return a new builder instance
     * @see WireContainerBuilder
     */
    @NotNull
    public static WireContainerBuilder builder() {
        return WireContainerBuilder.create();
    }

    /**
     * Returns a new {@link WireContainerBuilder} for creating and configuring a WireContext
     * with the provided environment.
     *
     * @param environment the environment to use
     * @return a new builder instance
     * @see WireContainerBuilder
     */
    @NotNull
    public static WireContainerBuilder builder(Environment environment) {
        return WireContainerBuilder.create(environment);
    }

    @NotNull
    public ScopeRegistry scopeRegistry() {
        return scopeRegistry;
    }

    @NotNull
    public WireContainerInitializer initializer() {
        return initializer;
    }

    /**
     * Whether this WireContainer is loaded.
     * <p>
     * If this method returns true, the underlying {@link WireContainerInitializer} is loaded and ready to be used.
     * If it returns false, you can load the repository by calling {@link #load()}
     *
     * @return true, if the {@link WireContainerInitializer} is loaded, otherwise false.
     */
    public boolean isLoaded() {
        return scopeRegistry.isInitialized();
    }

    /**
     * Whether this WireContainer is not loaded.
     * <p>
     * This is the opposite of {@link #isLoaded()}
     *
     * @return true, if the {@link WireContainerInitializer} is not yet loaded, otherwise false.
     */
    public boolean isNotLoaded() {
        return !isLoaded();
    }

    /**
     * The {@link Environment} relating to this WireContainer.
     * <p>
     * If not explicitly defined while constructing this repository, it's a completely standalone environment.
     * <p>
     * The {@link Environment} of a repository is immutable and can't be changed.
     * It must be explicitly defined when constructing the repository.
     * For details about how to set the {@link Environment}, see the create and open functions.
     *
     * @return the environment of this repository
     * @see #create()
     * @see #create(Environment)
     * @see #open()
     * @see #open(Environment)
     */
    @NotNull
    public Environment environment() {
        return environment;
    }

    /**
     * Returns the {@link OnDemandInjector} for this WireContainer.
     * <p>
     * The {@link OnDemandInjector} is instantiated lazily as it fulfills a special purpose.
     * It uses reflections to construct a class that is unknown at compile-time.
     * <p>
     * For more details about the {@link OnDemandInjector} please have a look at its documentation.
     *
     * @return the {@link OnDemandInjector} of this WireContainer
     * @see OnDemandInjector
     */
    @NotNull
    public OnDemandInjector onDemandInjector() {
        return onDemandInjector.get();
    }

    /**
     * Returns the {@link ExceptionHandlerContext} linked to this WireContainer.
     * <p>
     * You can manually modify it, but in general this is unnecessary.
     * Provide a Bean of the {@link ExceptionHandler} interface and it will be registered automatically.
     *
     * @return The {@link ExceptionHandlerContext} linked to this WireContainer
     * @see ExceptionHandlerContext
     * @see ExceptionHandler
     */
    @NotNull
    public ExceptionHandlerContext exceptionHandler() {
        return exceptionHandler;
    }

    /**
     * Returns the {@link StartupDiagnostics} linked to this WireContainer.
     *
     * @return The {@link StartupDiagnostics} linked to this WireContainer
     */
    @NotNull
    public StartupDiagnostics startupDiagnostics() {
        return startupDiagnostics;
    }

    /**
     * Announces a new object to be maintained in this WireContainer.
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
     * Announces an {@link IdentifiableProvider} to be maintained in this WireContainer.
     * <p>
     * The provider is respected in any later {@code get} calls.
     * <p>
     * Before the provider is registered, its {@link LoadCondition} is evaluated.
     * If it doesn't match, the method will not register the provider and instead return false.
     * Otherwise, the provider will be passed to the {@link WireContainerInitializer}, where it will be respected from now on.
     * <p>
     * This method exists to allow manual modifications of the WireContainer.
     * One of the use cases for this is the integration of existing IOC containers into WireDI.
     * <p>
     * You can combine this method with the {@link WireContainerCallback} to register the IdentifiableProvider
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

        scopeRegistry.registerProvider(identifiableProvider);
        return true;
    }

    /**
     * Loads the wireContainer, and thereby the underlying {@link WireContainerInitializer}.
     *
     * @return the time it took to load the WireContainer.
     */
    @NotNull
    public Timed load() {
        return load((w) -> LoadConfig.DEFAULT);
    }

    /**
     * Loads the wireContainer, and thereby the underlying {@link WireContainerInitializer}.
     * <p>
     * If the {@code loadConfigFunction} function returns true, the repository automatically loads all providers
     * implementing the {@link Eager} interface.
     *
     * @return the time it took to load the WireContainer.
     */
    public Timed load(Function<WireContainer, LoadConfig> loadConfigFunction) {
        is(isNotLoaded(), () -> "The WireContainer is already loaded");
        logger.debug(() -> "Loading WireContainer");
        return startupDiagnostics.measure("WireContainer.load", () -> {
                    announce(IdentifiableProvider.singleton(environment, TypeIdentifier.just(Environment.class)));
                    announce(IdentifiableProvider.singleton(environment.typeMapper(), TypeIdentifier.just(TypeMapper.class)));
                    announce(IdentifiableProvider.singleton(environment.resourceLoader(), TypeIdentifier.just(ResourceLoader.class)));
                    announce(IdentifiableProvider.singleton(environment.propertyLoader(), TypeIdentifier.just(PropertyLoader.class)));
                    announce(IdentifiableProvider.singleton(startupDiagnostics, TypeIdentifier.just(StartupDiagnostics.class)));

                    announce(IdentifiableProvider.singleton(this, TypeIdentifier.just(WireContainer.class)));
                    announce(IdentifiableProvider.singleton(exceptionHandler, TypeIdentifier.just(ExceptionHandlerContext.class)));

                    initializer.initialize(this);
                    LoadConfig loadConfig = loadConfigFunction.apply(this);

                    if (loadConfig.initializeEagerBeans) {
                        startupDiagnostics.measure("WireContainer.initializeEagerBeans", this::initializeEagerBeans);
                    }

                    if (loadConfig.synchronizeOnStates) {
                        startupDiagnostics.measure("WireContainer.synchronizeOnStates", () -> synchronizeOnStates(loadConfig.stateFullMaxTimeout));
                    }
                }).then(time -> logger.debug(() -> "The WireContainer has been loaded in " + time))
                .then(startupDiagnostics::seal);
    }

    private Timed initializeEagerBeans() {
        return Timed.of(() -> {
            logger.trace(() -> "Checking for eager classes");
            final Collection<Eager> eagerInstances = getAll(Eager.class);
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
            final Collection<StateFull<?>> stateFulls = getAll(TypeIdentifier.just(StateFull.class).cast());
            final StateFullInitializer stateFullInitializer = tryGet(StateFullInitializer.class).orElse(new StateFullInitializer.ParallelStream());
            if (!stateFulls.isEmpty()) {
                logger.debug(() -> "Synchronizing on " + stateFulls.size() + " StateFull instances.");
                stateFullInitializer.initialize(this, stateFulls, timeout);
            }
        });
    }

    public Timed clear() {
        is(isLoaded(), () -> "The WiredApplication is not loaded");
        logger.debug(() -> "Clearing the WireContainer");
        return Timed.of(() -> {
            scopeRegistry.tearDown();
            onDemandInjector.ifPresent(OnDemandInjector::clear);
        }).then(time -> logger.debug(() -> "The WireContainer has been cleared in " + time));
    }

    /**
     * Method to determine if a certain type is maintained in this repository.
     *
     * @param type the type to search for
     * @return true, if a bean is registered, otherwise false.
     */
    public boolean contains(@NotNull final Class<?> type) {
        return contains(TypeIdentifier.of(type));
    }

    public boolean contains(@NotNull final TypeIdentifier<?> type) {
        if (type.isAssignableFrom(IdentifiableProvider.class)) {
            return scopeRegistry.determineScopeOf(type).contains(type.getGenericTypes().getFirst());
        } else {
            return scopeRegistry.determineScopeOf(type).contains(type);
        }
    }

    public boolean contains(@NotNull final QualifiedTypeIdentifier<?> qualifiedType) {
        return contains(qualifiedType.type());
    }

    public <T> Search<T> search(@NotNull final TypeIdentifier<T> type) {
        return new Search<>(type);
    }

    /* ############ Try Get methods ############ */
    public <T> Optional<T> tryGet(@NotNull final Class<T> type) {
        return tryGet(TypeIdentifier.of(type));
    }

    public <T> Optional<T> tryGet(@NotNull final TypeIdentifier<T> type) {
        return scopeRegistry.determineScopeOf(type).tryGet(type);
    }

    public <T> Optional<T> tryGet(@NotNull final QualifiedTypeIdentifier<T> qualifiedTypeIdentifier) {
        return scopeRegistry.determineScopeOf(qualifiedTypeIdentifier).tryGet(qualifiedTypeIdentifier);
    }

    /* ############ Get methods ############ */
    public <T> T get(@NotNull final Class<T> type) {
        return get(TypeIdentifier.of(type));
    }

    public <T> T get(@NotNull final TypeIdentifier<T> type) {
        return scopeRegistry.determineScopeOf(type).get(type);
    }

    public <T> T get(@NotNull final QualifiedTypeIdentifier<T> type) {
        return scopeRegistry.determineScopeOf(type).get(type);
    }

    /* ############ Get all methods ############ */
    public <T> List<T> getAll(Class<T> type) {
        return getAll(TypeIdentifier.of(type));
    }

    public <T> List<T> getAll(TypeIdentifier<T> type) {
        return scopeRegistry.getAllInstances(type);
    }

    /* ############ Get provider methods ############ */
    @NotNull
    public <T> Provider<T> getProvider(@NotNull final Class<T> type) {
        return getProvider(TypeIdentifier.of(type));
    }

    @NotNull
    public <T> Provider<T> getProvider(@NotNull final TypeIdentifier<T> type) {
        return toProvider(getNativeProvider(type));
    }

    @NotNull
    public <T> Provider<T> getProvider(QualifiedTypeIdentifier<T> type) {
        return toProvider(getNativeProvider(type));
    }

    /* ############ Get Native Provider methods ############ */
    public <T> IdentifiableProvider<T> getNativeProvider(TypeIdentifier<T> typeIdentifier) {
        return scopeRegistry.determineScopeOf(typeIdentifier).getProvider(typeIdentifier);
    }

    public <T> IdentifiableProvider<T> getNativeProvider(QualifiedTypeIdentifier<T> typeIdentifier) {
        return scopeRegistry.determineScopeOf(typeIdentifier.type()).getProvider(typeIdentifier);
    }

    /* ############ Get ObjectReference methods ############ */
    public <T> ObjectReference<T> getReference(@NotNull final Class<T> type) {
        return getReference(TypeIdentifier.of(type));
    }

    public <T> ObjectReference<T> getReference(@NotNull final TypeIdentifier<T> type) {
        return new ObjectReference<>(this, type);
    }

    /* ############ Get Bean methods ############ */
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

    public static class Search<T> {

        private Scope scope;
        private QualifierType qualifierType;
        private final TypeIdentifier<T> typeIdentifier;

        public Search(TypeIdentifier<T> typeIdentifier) {
            this.typeIdentifier = typeIdentifier;
        }

        public Search<T> withQualifier(QualifierType qualifierType) {
            this.qualifierType = qualifierType;
            return this;
        }

        public T find() {
            if (qualifierType != null) {
                return scope.get(typeIdentifier.qualified(qualifierType));
            } else {
                return scope.get(typeIdentifier);
            }
        }

        public Optional<T> tryFind() {
            if (qualifierType != null) {
                return scope.tryGet(typeIdentifier.qualified(qualifierType));
            } else {
                return scope.tryGet(typeIdentifier);
            }
        }
    }
}
