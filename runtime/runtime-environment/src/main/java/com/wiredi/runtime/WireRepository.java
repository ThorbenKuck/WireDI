package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.async.StateFull;
import com.wiredi.runtime.async.state.State;
import com.wiredi.runtime.banner.Banner;
import com.wiredi.runtime.beans.Bean;
import com.wiredi.runtime.beans.BeanContainer;
import com.wiredi.runtime.domain.*;
import com.wiredi.runtime.domain.errors.ExceptionHandler;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.provider.WrappingProvider;
import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import com.wiredi.runtime.environment.resolvers.EnvironmentExpressionResolver;
import com.wiredi.runtime.exceptions.BeanNotFoundException;
import com.wiredi.runtime.exceptions.DiInstantiationException;
import com.wiredi.runtime.properties.PropertyLoader;
import com.wiredi.runtime.properties.loader.PropertyFileTypeLoader;
import com.wiredi.runtime.qualifier.QualifierType;
import com.wiredi.runtime.resources.ResourceLoader;
import com.wiredi.runtime.resources.ResourceProtocolResolver;
import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.types.TypeMapper;
import com.wiredi.runtime.values.Value;
import jakarta.inject.Provider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.UndeclaredThrowableException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static com.wiredi.runtime.lang.Preconditions.is;

/**
 * The WireRepository is the central repository that holds all beans.
 * <p>
 * A WireRepository can be constructed in mainly two ways:
 *
 * <pre><code>
 * class Example {
 *     public void emptyRepository() {
 *         WireRepository wireRepository = WireRepository.create();
 *     }
 *
 *     public void loadedRepository() {
 *         WireRepository wireRepository = WireRepository.open();
 *     }
 * }
 * </code></pre>
 * <p>
 * A loaded Repository contains all {@link IdentifiableProvider} instances that are resolvable using the
 * {@link java.util.ServiceLoader}.
 * <p>
 * An empty Repository is exactly that, completely empty.
 * <p>
 * <h2>Loaded Repository</h2>
 * <p>
 * A loaded repository is not only filled, it is also setup correctly. That means:
 * <ul>
 *     <li>The {@link Environment} is completely setup</li>
 *     <li>The {@link BeanContainer} is loaded and can uniquely resolve a {@link Bean}</li>
 *     <li>Any {@link Eager} class is loaded</li>
 *     <li>All {@link State} provided by {@link StateFull} instances in the {@link BeanContainer} have been successfully completed</li>
 * </ul>
 * This Repository can be used without any other additional configuration:
 * <pre><code>
 * WireRepository repository = WireRepository.open();
 * MyService service = repository.get(MyService.class);
 * </code></pre>
 * Constructing a loaded WireRepository using the <pre>open</pre> function, is synonymous to:
 * <pre><code>
 * WireRepository repository = WireRepository.create();
 * Timed loadTime = repository.load();
 * </code></pre>
 * <h2>Empty Repository</h2>
 * An empty repository is used if manually configuration is required before the default instances are loaded.
 * This could be a manual configuration of the Environment or the beans.
 * <p>
 * It might look like this:
 * <pre><code>
 * WireRepository repository = WireRepository.create();
 * preConfigure(repository.environment());
 * preConfigure(repository.beanContainer());
 * IdentifiableProvider myProvider = getMyProvider();
 * repository.announce(myProvider);
 * repository.load();
 * MyService service = repository.get(MyService.class);
 * </code></pre>
 * Optionally, you can call "load" at any point in time.
 * Calling load will trigger the process of loading and autoconfiguration as described in the section "Loaded Repository".
 * <pre><code>
 * WireRepository repository = WireRepository.create();
 * configure(repository);
 * repository.load(); // Trigger the loading process
 * afterLoadConfiguration(repository); // Additional configuration after loading
 * MyService service = repository.get(MyService.class);
 * </code></pre>
 *
 * @see BeanContainer
 * @see Environment
 */
public class WireRepository {

    @NotNull
    private static final Logging logger = Logging.getInstance(WireRepository.class);
    @NotNull
    private static final ServiceLoader loader = ServiceLoader.getInstance();
    @NotNull
    private final Environment environment = new Environment();
    @NotNull
    private final Banner banner = new Banner(environment);
    @NotNull
    private final BeanContainer beanContainer;
    @NotNull
    private final WireRepositoryProperties properties;
    @NotNull
    private final List<WireRepositoryContextCallbacks> contextCallbacks = new ArrayList<>();
    @NotNull
    private final Value<OnDemandInjector> onDemandInjector = Value.lazy(() -> new OnDemandInjector(this));
    @NotNull
    private final ExceptionHandlerContext exceptionHandler = new ExceptionHandlerContext(this);

    /**
     * Constructs a new WireRepository using the default {@link WireRepositoryProperties}
     */
    public WireRepository() {
        this(new WireRepositoryProperties());
    }

    /**
     * Constructs a new WireRepository using a custom {@link WireRepositoryProperties}.
     *
     * @param properties the properties for the WireRepository.
     */
    public WireRepository(WireRepositoryProperties properties) {
        if (properties.contextCallbacksEnabled()) {
            this.contextCallbacks.addAll(loader.contextCallbacks());
        } else {
            this.contextCallbacks.add(new LoggingWireRepositoryContextCallbacks());
        }

        this.beanContainer = new BeanContainer(properties, loader);
        this.properties = properties;
        new ArrayList<>(this.contextCallbacks).forEach(it -> it.initialize(this));
    }

    /**
     * Creates a new fully configured WireRepository using the default properties.
     * <p>
     * This method is recommended to be used in nearly all scenarios you could imagine.
     *
     * @return a new and fully configured WireRepository.
     */
    public static WireRepository open() {
        WireRepository repository = new WireRepository();
        repository.load();

        return repository;
    }

    /**
     * Creates a new fully configured WireRepository using the default properties.
     * <p>
     * It behaves similar to {@link #open()}, but allows you to provide a new instance of the properties.
     *
     * @param properties the properties to configure the WireRepository
     * @return a new and fully configured WireRepository.
     */
    public static WireRepository open(WireRepositoryProperties properties) {
        WireRepository repository = new WireRepository(properties);
        repository.load();

        return repository;
    }

    /**
     * Creates a new, not loaded WireRepository.
     *
     * @return a new WireRepository instance
     */
    public static WireRepository create() {
        return new WireRepository();
    }

    /**
     * Creates a new, not loaded WireRepository.
     * <p>
     * It behaves similar to {@link #create()}, but allows you to provide a new instance of the properties.
     *
     * @param properties the properties to configure the WireRepository
     * @return a new WireRepository instance
     */
    public static WireRepository create(WireRepositoryProperties properties) {
        return new WireRepository(properties);
    }

    /**
     * The concrete Banner that is set up for this WireRepository
     *
     * @return the Banner for this WireRepository
     */
    public Banner banner() {
        return banner;
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
     * Returns the {@link Environment} of this WireRepository.
     * <p>
     * This Environment contains configurations and properties.
     * For further details, please refer to the {@link Environment} documentation.
     *
     * @return the Environment for this repository
     * @see Environment
     */
    @NotNull
    public Environment environment() {
        return this.environment;
    }    @NotNull
    private final ShutdownHook shutdownHook = new ShutdownHook(() -> {
        if (isLoaded()) {
            logger.info("Detected JVM shutdown. Instructing the WireRepository to shutdown.");
            destroy();
        }
    });

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
     * The provider will be respected in any later call {@code get} calls.
     * <p>
     * Before the provider is registered, its {@link LoadCondition} is evaluated.
     * If it does not match, the method will not register the provider and instead return false.
     * Otherwise, the provider will be passed to the {@link BeanContainer}, where it will be respected from now on.
     * <p>
     * This method exists to allow manual modifications of the WireRepository.
     * One of the use cases for this is the integration of existing IOC containers into WireDI.
     * <p>
     * You can combine this method with the {@link WireRepositoryContextCallbacks} to register the IdentifiableProvider
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
            if (!condition.matches(this)) {
                return false;
            }
        }

        beanContainer.register(identifiableProvider);
        return true;
    }

    /**
     * Registers a new {@link WireRepositoryContextCallbacks} to be invoked by this repository.
     *
     * @param contextCallbacks the callback that should be registered
     * @see WireRepositoryContextCallbacks
     */
    public void register(@NotNull WireRepositoryContextCallbacks contextCallbacks) {
        this.contextCallbacks.add(contextCallbacks);
    }

    /**
     * Destroys and therefor shuts down this instance of the {@link WireRepository}.
     * <p>
     * This method will make sure to tear down all hold {@link StateFull} and {@link Disposable} instances.
     * <p>
     * After this, resources will be cleaned up, which includes {@link BeanContainer} and {@link Environment}
     *
     * @return The time it took to destroy the instance
     * @see Timed
     * @see StateFull
     * @see Disposable
     * @see #load()
     */
    @NotNull
    public Timed destroy() {
        is(isLoaded(), () -> "The WireRepository is not loaded");
        return safeTimed(() -> {
            logger.debug(() -> "Destroying all Beans of the WireRepository");
            getAll(TypeIdentifier.just(StateFull.class))
                    .parallelStream()
                    .forEach(StateFull::tearDown);
            getAll(TypeIdentifier.just(Disposable.class))
                    .parallelStream()
                    .forEach(bean -> bean.tearDown(this));
            beanContainer.clear();
            environment.clear();
            onDemandInjector.ifPresent(OnDemandInjector::clear);
            new ArrayList<>(this.contextCallbacks).forEach(callback -> callback.destroyed(this));
            if (Thread.currentThread() != shutdownHook) {
                Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
            }
            // Suggest to the gc freeing up resources is a good idea.
            // We have just cleared a lot of collections, so the gc can pick up a lot.
            Runtime.getRuntime().gc();
        }).then(timed -> logger.info(() -> "WireRepository destroyed in " + timed));
    }

    /**
     * Sets up the WireRepository.
     * <p>
     * It will run through the following steps:
     * <ul>
     *     <li>Setting up the environment: The environment will be autoconfigured</li>
     *     <li>Setting up the BeanContainer: Setup all beans and construct the IdentifiableProviders if not already instantiated</li>
     *     <li>Additional environment configuration: The environment will be configured with bean instances</li>
     *     <li>Setting up eager classes: Classes implementing the {@link Eager} interface will be invoked</li>
     *     <li>Synchronization: Classes implementing the {@link StateFull} interface will be asked to provide states to synchronize on all states</li>
     * </ul>
     *
     * @return a new {@link Timed} instance containing the time it took to load the repository
     */
    @NotNull
    public Timed load() {
        is(isNotLoaded(), () -> "The WireRepository is already loaded");
        return safeTimed(() -> {
            contextCallbacks.forEach(it -> it.loadingStarted(this));

            Timed.of(() -> {
                logger.debug("Loading Environment");
                setupEnvironment();
            }).then(timed -> contextCallbacks.forEach(it -> it.loadedEnvironment(timed, environment)));

            Timed.of(() -> {
                logger.debug("Loading BeanContainer");
                setupBeanContainer();
            }).then(timed -> contextCallbacks.forEach(callback -> callback.loadedBeanContainer(timed, this, beanContainer)));

            Timed.of(() -> {
                logger.debug("Configuring Environment with Bean instances");
                configureEnvironment();
            }).then(timed -> contextCallbacks.forEach(callback -> callback.configuredEnvironment(timed, environment)));

            if (properties.loadEagerInstance()) {
                setupEagerClasses(contextCallbacks);
            }

            if (properties.awaitStates()) {
                synchronizeOnStates();
            }
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }).then(totalTime -> contextCallbacks.forEach(callback -> callback.loadingFinished(totalTime, this)));
    }

    /**
     * Configures the environment and prepares it for the runtime
     */
    private void setupEnvironment() {
        environment.autoconfigure();
        banner.print();
        environment.printProfiles();
    }

    private void configureEnvironment() {
        environment.addExpressionResolvers(getAll(EnvironmentExpressionResolver.class));
        environment.resourceLoader().addProtocolResolvers(getAll(ResourceProtocolResolver.class));
        environment.propertyLoader().addPropertyFileLoaders(getAll(PropertyFileTypeLoader.class));
        getAll(EnvironmentConfiguration.class).forEach(it -> it.configure(environment));
    }

    private void setupBeanContainer() {
        logger.trace("Registering all known static types");
        announce(IdentifiableProvider.singleton(beanContainer, TypeIdentifier.just(BeanContainer.class)));
        announce(IdentifiableProvider.singleton(this, TypeIdentifier.just(WireRepository.class)));
        announce(IdentifiableProvider.singleton(banner, TypeIdentifier.just(Banner.class)));
        announce(IdentifiableProvider.singleton(environment, TypeIdentifier.just(Environment.class)));
        announce(IdentifiableProvider.singleton(environment.resourceLoader(), TypeIdentifier.just(ResourceLoader.class)));
        announce(IdentifiableProvider.singleton(environment.propertyLoader(), TypeIdentifier.just(PropertyLoader.class)));
        announce(IdentifiableProvider.singleton(exceptionHandler, TypeIdentifier.just(ExceptionHandlerContext.class)));
        announce(IdentifiableProvider.singleton(environment.typeMapper(), TypeIdentifier.just(TypeMapper.class)));
        beanContainer.load(this);
    }

    private void setupEagerClasses(List<WireRepositoryContextCallbacks> contextCallbacks) {
        logger.debug("Checking for eager classes");
        final List<Eager> eagerInstances = getAll(Eager.class);
        if (!eagerInstances.isEmpty()) {
            logger.debug("Loading " + eagerInstances.size() + " eager classes.");
            Timed.of(() -> eagerInstances.parallelStream().forEach(it -> it.setup(this)))
                    .then(timed -> contextCallbacks.forEach(callback -> callback.loadedEagerClasses(timed, this, eagerInstances)));
        }
    }

    private void synchronizeOnStates() {
        // Writing StateFull<?> right here leads to compile time errors, this
        // is why we explicitly skip the raw type inspection with the following comment
        //noinspection rawtypes
        final List<StateFull> stateFulls = getAll(TypeIdentifier.just(StateFull.class));
        if (!stateFulls.isEmpty()) {
            logger.debug(() -> "Synchronizing on " + stateFulls.size() + " StateFull instances.");
            Duration duration = properties.awaitStatesTimeout();
            Timed.of(() -> stateFulls.parallelStream().forEach(stateFull -> stateFull.getState().awaitUntilSet(duration)))
                    .then(timed -> contextCallbacks.forEach(callback -> callback.synchronizedOnStates(timed, this, stateFulls)));
        }
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
        if (type.isAssignableFrom(IdentifiableProvider.class)) {
            return beanContainer.get(type.getGenericTypes().getFirst()).isPresent();
        } else {
            return !beanContainer.getAll(type).isEmpty();
        }
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

    @NotNull
    public Timed safeTimed(@NotNull Runnable runnable) {
        return Timed.of(() -> safeRun(runnable));
    }

    public void safeRun(@NotNull Runnable runnable) {
        try {
            runnable.run();
        } catch (final Throwable throwable) {
            try {
                exceptionHandler.handle(throwable);
            } catch (final Throwable e) {
                throw new IllegalStateException(e);
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

    private static final class ShutdownHook extends Thread {
        public ShutdownHook(Runnable runnable) {
            super(runnable);
            setName("WireRepository Shutdown Hook");
            setPriority(Thread.NORM_PRIORITY + 2);
            setDaemon(false);
        }
    }
}
