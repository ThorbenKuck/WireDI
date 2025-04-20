package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.application.ShutdownHook;
import com.wiredi.runtime.async.StateFull;
import com.wiredi.runtime.async.barriers.Barrier;
import com.wiredi.runtime.async.barriers.MutableBarrier;
import com.wiredi.runtime.banner.Banner;
import com.wiredi.runtime.domain.Disposable;
import com.wiredi.runtime.domain.WireRepositoryContextCallbacks;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import com.wiredi.runtime.environment.resolvers.EnvironmentExpressionResolver;
import com.wiredi.runtime.properties.loader.PropertyFileTypeLoader;
import com.wiredi.runtime.resources.ResourceProtocolResolver;
import com.wiredi.runtime.time.Timed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.wiredi.runtime.lang.Preconditions.is;
import static com.wiredi.runtime.lang.Preconditions.isNot;

public class WiredApplicationInstance {

    private static final Logging logger = Logging.getInstance(WiredApplicationInstance.class);
    @NotNull
    private final WireRepository wireRepository;
    @NotNull
    private final Environment environment;
    @NotNull
    private final Barrier barrier;
    @NotNull
    private final Banner banner;
    private final ServiceLoader serviceLoader = ServiceLoader.getInstance();
    private final List<WireRepositoryContextCallbacks> contextCallbacks = serviceLoader.contextCallbacks();
    private boolean isRunning = false;

    public WiredApplicationInstance(
            @NotNull WireRepository wireRepository,
            @NotNull Barrier barrier
    ) {
        this.wireRepository = wireRepository;
        this.environment = wireRepository.environment();
        this.barrier = barrier;
        this.banner = new Banner(wireRepository.environment());
    }

    Timed runStartupRoutine() {
        isNot(isRunning, () -> "The WiredApplication is already started");
        return Timed.of(() -> {
                    contextCallbacks.addAll(wireRepository.getAll(WireRepositoryContextCallbacks.class));
                    contextCallbacks.forEach(it -> it.loadingStarted(wireRepository));

                    Timed.of(() -> {
                        logger.debug(() -> "Loading Environment");
                        environment.autoconfigure();
                        banner.print();
                        environment.printProfiles();
                    }).then(timed -> contextCallbacks.forEach(it -> it.loadedEnvironment(timed, environment)));

                    if (wireRepository.isNotLoaded()) {
                        logger.trace(() -> "Registering all known static types");
                        wireRepository.announce(IdentifiableProvider.singleton(banner, TypeIdentifier.just(Banner.class)));
                        logger.debug(() -> "Loading WireRepository");

                        wireRepository.load(repository -> {
                            logger.debug(() -> "Configuring Environment with Bean instances");
                            environment.addExpressionResolvers(wireRepository.getAll(EnvironmentExpressionResolver.class));
                            environment.resourceLoader().addProtocolResolvers(wireRepository.getAll(ResourceProtocolResolver.class));
                            environment.propertyLoader().addPropertyFileLoaders(wireRepository.getAll(PropertyFileTypeLoader.class));
                            wireRepository.getAll(EnvironmentConfiguration.class).forEach(it -> it.configure(environment));

                            return environment.getProperty(PropertyKeys.LOAD_EAGER_INSTANCES.getKey(), true);
                        }).then(time -> logger.info("WireRepository loaded in " + time));
                    }

                    if (environment.getProperty(PropertyKeys.AWAIT_STATES.getKey(), true)) {
                        Duration timeout = environment.getProperty(PropertyKeys.AWAIT_STATES_TIMEOUT.getKey(), Duration.class)
                                .orElse(null);
                        synchronizeOnStates(timeout);
                    }
                    Runtime.getRuntime().addShutdownHook(shutdownHook);
                    isRunning = true;
                })
                .then(totalTime -> contextCallbacks.forEach(callback -> callback.loadingFinished(totalTime, wireRepository)));
    }

    @NotNull
    public Timed shutdown() {
        is(isRunning, () -> "The WiredApplication is not started");
        return Timed.of(() -> {
                    List<WireRepositoryContextCallbacks> contextCallbacks = wireRepository.getAll(WireRepositoryContextCallbacks.class);
                    logger.debug(() -> "Destroying all Beans of the WireRepository");
                    wireRepository.getAll(TypeIdentifier.just(StateFull.class))
                            .parallelStream()
                            .forEach(StateFull::dismantleState);
                    wireRepository.getAll(TypeIdentifier.just(Disposable.class))
                            .parallelStream()
                            .forEach(bean -> bean.tearDown(wireRepository));
                    environment.clear();
                    new ArrayList<>(contextCallbacks).forEach(callback -> callback.destroyed(wireRepository));
                    wireRepository.getAll(ShutdownListener.class).forEach(it -> it.tearDown(wireRepository));
                    wireRepository.clear();
                    if (Thread.currentThread() != shutdownHook) {
                        Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
                    }
                    // Suggest to the gc freeing up resources is a good idea.
                    // We have just cleared a lot of collections, so the gc can pick up a lot of things.
                    Runtime.getRuntime().gc();
                })
                .then(timed -> logger.info(() -> "WireRepository destroyed in " + timed))
                .then(() -> isRunning = false);
    }

    private void synchronizeOnStates(@Nullable Duration timeout) {
        // Writing StateFull<?> right here leads to compile time errors, this
        // is why we explicitly skip the raw type inspection with the following comment
        logger.trace(() -> "Synchronizing in states");
        //noinspection rawtypes
        final List<StateFull> stateFulls = wireRepository.getAll(TypeIdentifier.just(StateFull.class));
        if (!stateFulls.isEmpty()) {
            logger.debug(() -> "Synchronizing on " + stateFulls.size() + " StateFull instances.");
            if (timeout != null) {
                stateFulls.parallelStream().forEach(stateFull -> stateFull.getState().awaitUntilSet(timeout));
            } else {
                stateFulls.parallelStream().forEach(stateFull -> stateFull.getState().awaitUntilSet());
            }
        }
    }

    public boolean isCompleted() {
        return barrier.isOpen();
    }

    public boolean isWaitingForCompletion() {
        return barrier.isClosed();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isNotRunning() {
        return !isRunning;
    }

    public void awaitCompletion() {
        barrier.traverse();
    }

    public WireRepository wireRepository() {
        return wireRepository;
    }

    public Banner banner() {
        return banner;
    }

    public Environment environment() {
        return environment;
    }

    static class ShutdownListenerProvider implements IdentifiableProvider<ShutdownListener> {

        private static final TypeIdentifier<ShutdownListener> TYPE = TypeIdentifier.of(ShutdownListener.class);
        private static final List<TypeIdentifier<?>> ADDITIONAL_TYPES = List.of(TypeIdentifier.of(Disposable.class));
        private final ShutdownListener instance;

        ShutdownListenerProvider(MutableBarrier barrier) {
            this.instance = new ShutdownListener(barrier);
        }

        @Override
        public @NotNull TypeIdentifier<? super ShutdownListener> type() {
            return TYPE;
        }

        @Override
        public @NotNull List<TypeIdentifier<?>> additionalWireTypes() {
            return ADDITIONAL_TYPES;
        }

        @Override
        public @Nullable ShutdownListener get(@NotNull WireRepository wireRepository, @NotNull TypeIdentifier<ShutdownListener> concreteType) {
            return instance;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ShutdownListenerProvider that)) return false;
            return Objects.equals(instance, that.instance);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(instance);
        }

        @Override
        public String toString() {
            return "ShutdownListenerProvider{" +
                    "instance=" + instance +
                    '}';
        }
    }

    static class ShutdownListener implements Disposable {

        private final MutableBarrier barrier;

        ShutdownListener(MutableBarrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void tearDown(WireRepository origin) {
            if (barrier.isClosed()) {
                logger.info(() -> "Application shutdown detected");
                barrier.open();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ShutdownListener that)) return false;
            return Objects.equals(barrier, that.barrier);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(barrier);
        }

        @Override
        public String toString() {
            return "ShutdownListener{" +
                    "barrier=" + barrier +
                    '}';
        }
    }

    private final ShutdownHook shutdownHook = new ShutdownHook(() -> {
        if (isRunning) {
            logger.info(() -> "Detected JVM shutdown. Instructing the WireRepository to shutdown.");
            shutdown();
        }
    });
}
