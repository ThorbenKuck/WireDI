package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.async.StateFull;
import com.wiredi.runtime.async.state.State;
import com.wiredi.runtime.banner.Banner;
import com.wiredi.runtime.beans.Bean;
import com.wiredi.runtime.beans.BeanContainer;
import com.wiredi.runtime.domain.*;
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
 */
public class WireRepository {

    @NotNull
    private static final Logging logger = Logging.getInstance(WireRepository.class);
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
    private final ExceptionHandler exceptionHandler = new ExceptionHandler(this);
    @NotNull
    private final ShutdownHook shutdownHook = new ShutdownHook(() -> {
        if (isLoaded()) {
            logger.info("Detected JVM shutdown. Instructing the WireRepository to shutdown.");
            destroy();
        }
    });
    private final ServiceLoader loader;

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
        this.loader = ServiceLoader.getInstance();
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

    public static WireRepository open(WireRepositoryProperties properties) {
        WireRepository repository = new WireRepository(properties);
        repository.load();

        return repository;
    }

    public static WireRepository create() {
        return new WireRepository();
    }

    public static WireRepository create(WireRepositoryProperties properties) {
        return new WireRepository(properties);
    }

    /**
     * The concrete Banner that is setup for this WireRepository
     *
     * @return
     */
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
    public BeanContainer beanContainer() {
        return beanContainer;
    }

    @NotNull
    public OnDemandInjector onDemandInjector() {
        return onDemandInjector.get();
    }

    public ExceptionHandler exceptionHandler() {
        return exceptionHandler;
    }

    public <T> void announce(@NotNull final T o) {
        announce(IdentifiableProvider.singleton(o));
    }

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

    public void register(@NotNull WireRepositoryContextCallbacks contextCallbacks) {
        this.contextCallbacks.add(contextCallbacks);
    }

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
            if (onDemandInjector.isSet()) {
                onDemandInjector.get().clear();
            }
            new ArrayList<>(this.contextCallbacks).forEach(callback -> callback.destroyed(this));
        }).then(timed -> logger.info(() -> "WireRepository destroyed in " + timed));
    }

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
        announce(IdentifiableProvider.singleton(exceptionHandler, TypeIdentifier.just(ExceptionHandler.class)));
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

    public void synchronizeOnStates() {
        logger.debug("Synchronizing on states");

        // Writing StateFull<?> right here leads to compile time errors, this
        // is why we explicitly skip the raw type inspection with the following comment
        //noinspection rawtypes
        final List<StateFull> stateFulls = getAll(TypeIdentifier.just(StateFull.class));
        if (!stateFulls.isEmpty()) {
            Duration duration = properties.awaitStatesTimeout();
            Timed.of(() -> stateFulls.parallelStream()
                    .map(StateFull::getState)
                    .forEach(state -> state.awaitUntilSet(duration))
            ).then(timed -> contextCallbacks.forEach(callback -> callback.synchronizedOnStates(timed, this, stateFulls)));
        }
    }

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
                exceptionHandler.handleError(throwable);
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
                exceptionHandler.handleError(throwable);
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
            throw wireCreationError(e, type, provider);
        }
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

    private static final class ShutdownHook extends Thread {
        public ShutdownHook(Runnable runnable) {
            super(runnable);
            setName("WireRepository Shutdown Hook");
            setPriority(Thread.NORM_PRIORITY + 2);
            setDaemon(false);
        }
    }
}
