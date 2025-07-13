package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.application.ShutdownHook;
import com.wiredi.runtime.async.StateFull;
import com.wiredi.runtime.async.barriers.Barrier;
import com.wiredi.runtime.async.barriers.MutableBarrier;
import com.wiredi.runtime.banner.Banner;
import com.wiredi.runtime.domain.Disposable;
import com.wiredi.runtime.domain.WireContainerCallback;
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
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.wiredi.runtime.PropertyKeys.PRINT_DIAGNOSTICS;
import static com.wiredi.runtime.lang.Preconditions.is;
import static com.wiredi.runtime.lang.Preconditions.isNot;

/**
 * Represents a running instance of a wired application.
 * <p>
 * This class is responsible for managing the lifecycle of a WireDI application, including:
 * <ul>
 *   <li>Starting up the application and loading the {@link WireContainer}</li>
 *   <li>Synchronizing {@link StateFull} instances during startup</li>
 *   <li>Managing the application's completion state through a {@link Barrier}</li>
 *   <li>Shutting down the application and cleaning up resources</li>
 * </ul>
 * <p>
 * Instances of this class are typically created by the {@link WiredApplication} class
 * and shouldn't be created directly.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Start an application and get the instance
 * WiredApplicationInstance instance = WiredApplication.start();
 * 
 * // Check if the application is running
 * if (instance.isRunning()) {
 *     // Do something while the application is running
 * }
 * 
 * // Wait for the application to complete
 * instance.awaitCompletion();
 * 
 * // Shut down the application
 * instance.shutdown();
 * }</pre>
 * 
 * @see WiredApplication
 * @see WireContainer
 * @see Barrier
 */
public class WiredApplicationInstance {

    private static final Logging logger = Logging.getInstance(WiredApplicationInstance.class);
    @NotNull
    private final WireContainer wireContainer;
    @NotNull
    private final Environment environment;
    @NotNull
    private final Barrier barrier;
    @NotNull
    private final Banner banner;
    private final List<WireContainerCallback> contextCallbacks;
    private boolean isRunning = false;

    /**
     * Creates a new WiredApplicationInstance.
     * <p>
     * This constructor is typically called by the {@link WiredApplication} class
     * and should not be called directly.
     *
     * @param wireContainer the wire container to use for this application instance
     * @param barrier the barrier to use for managing the application's completion state
     */
    public WiredApplicationInstance(
            @NotNull WireContainer wireContainer,
            @NotNull Barrier barrier
    ) {
        this.wireContainer = wireContainer;
        this.environment = wireContainer.environment();
        this.barrier = barrier;
        this.banner = new Banner(wireContainer.environment());
        contextCallbacks = ServiceFiles.getInstance(WireContainerCallback.class)
                .tryInitialize(wireContainer.onDemandInjector())
                .instances();
    }

    /**
     * Runs the startup routine for this application instance.
     * <p>
     * This method is called by the {@link WiredApplication} class during application startup.
     * It performs the following tasks:
     * <ul>
     *   <li>Loads the environment and prints the banner</li>
     *   <li>Loads the wire repository if it's not already loaded</li>
     *   <li>Synchronizes {@link StateFull} instances if configured to do so</li>
     *   <li>Registers a shutdown hook to clean up resources when the JVM terminates</li>
     * </ul>
     * <p>
     * This method is package-private, so that it can be run by the {@link WiredApplication} class,
     * but it isn't recommended to run it directly.
     *
     * @return a {@link Timed} instance containing timing information for the startup routine
     */
    @NotNull Timed start() {
        isNot(isRunning, () -> "The WiredApplication is already started");
        return Timed.of(() -> {
                    contextCallbacks.addAll(wireContainer.getAll(WireContainerCallback.class));
                    contextCallbacks.forEach(it -> it.loadingStarted(wireContainer));

                    Timed.of(() -> {
                        logger.debug(() -> "Loading Environment");
                        environment.autoconfigure();
                        banner.print();
                        environment.printProfiles();
                    }).then(timed -> contextCallbacks.forEach(it -> it.loadedEnvironment(timed, environment)));

                    if (wireContainer.isNotLoaded()) {
                        logger.trace(() -> "Registering all known static types");
                        wireContainer.announce(IdentifiableProvider.singleton(banner, TypeIdentifier.just(Banner.class)));
                        logger.debug(() -> "Loading WireContainer");

                        wireContainer.load(repository -> {
                            logger.debug(() -> "Configuring Environment with Bean instances");
                            environment.addExpressionResolvers(wireContainer.getAll(EnvironmentExpressionResolver.class));
                            environment.resourceLoader().addProtocolResolvers(wireContainer.getAll(ResourceProtocolResolver.class));
                            environment.propertyLoader().addPropertyFileLoaders(wireContainer.getAll(PropertyFileTypeLoader.class));
                            wireContainer.getAll(EnvironmentConfiguration.class).forEach(it -> it.configure(environment));

                            return new WireContainer.LoadConfig(
                                    environment.getProperty(PropertyKeys.LOAD_EAGER_INSTANCES.getKey(), true),
                                    environment.getProperty(PropertyKeys.AWAIT_STATES.getKey(), true),
                                    environment.getProperty(PropertyKeys.AWAIT_STATES_TIMEOUT.getKey(), Duration.class)
                                    .orElse(null)
                            );
                        }).then(time -> logger.info("WireContainer loaded in " + time));
                    }

                    Runtime.getRuntime().addShutdownHook(shutdownHook);
                    isRunning = true;
                })
                .then(totalTime -> contextCallbacks.forEach(callback -> callback.loadingFinished(totalTime, wireContainer)))
                .then(this::printDiagnostics);
    }

    private void printDiagnostics() {
        logger.debug(() -> "Printing WireContainer Diagnostics");
        if (environment.getProperty(PRINT_DIAGNOSTICS.getKey(), false)) {
            StartupDiagnostics startupDiagnostics = wireContainer.startupDiagnostics();
            StartupDiagnostics.TimingState state = startupDiagnostics.state();

            if (!state.isEmpty()) {
                List<String> lines = new ArrayList<>();
                printState(state, 0, lines);
                String diagnostics = String.join("\n", lines);
                logger.info(() -> "Startup diagnostics:\n" + diagnostics);
            }
        }
    }

    private void printState(StartupDiagnostics.TimingState state, int depth, List<String> lines) {
        lines.add(pad(" - " + state.name() + ": " + state.time(), depth));
        state.children().forEach(child -> printState(child, depth + 1, lines));
    }

    private String pad(String string, int depth) {
        return "  ".repeat(Math.max(0, depth)) + string;
    }

    /**
     * Shuts down this application instance.
     * <p>
     * This method performs the following tasks:
     * <ul>
     *   <li>Calls {@link StateFull#dismantleState()} on all {@link StateFull} instances</li>
     *   <li>Calls {@link Disposable#tearDown(WireContainer)} on all {@link Disposable} instances</li>
     *   <li>Clears the environment</li>
     *   <li>Notifies all context callbacks that the application is being destroyed</li>
     *   <li>Clears the wire repository</li>
     *   <li>Removes the shutdown hook if this method is not called from the shutdown hook</li>
     * </ul>
     *
     * @return a {@link Timed} instance containing timing information for the shutdown process
     * @throws IllegalStateException if the application is not running
     */
    @NotNull
    public Timed shutdown() {
        is(isRunning, () -> "The WiredApplication is not started");
        return Timed.of(() -> {
                    Collection<WireContainerCallback> contextCallbacks = wireContainer.getAll(WireContainerCallback.class);
                    logger.debug(() -> "Destroying all Beans of the WireContainer");
                    wireContainer.getAll(TypeIdentifier.just(StateFull.class))
                            .parallelStream()
                            .forEach(StateFull::dismantleState);
                    wireContainer.getAll(TypeIdentifier.just(Disposable.class))
                            .parallelStream()
                            .forEach(bean -> bean.tearDown(wireContainer));
                    environment.clear();
                    new ArrayList<>(contextCallbacks).forEach(callback -> callback.destroyed(wireContainer));
                    wireContainer.getAll(ShutdownListener.class).forEach(it -> it.tearDown(wireContainer));
                    wireContainer.clear();
                    if (Thread.currentThread() != shutdownHook) {
                        Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
                    }
                    // Suggest to the gc freeing up resources is a good idea.
                    // We have just cleared a lot of collections, so the gc can pick up a lot of things.
                    Runtime.getRuntime().gc();
                })
                .then(timed -> logger.info(() -> "WireContainer shutdown in " + timed))
                .then(() -> isRunning = false);
    }

    /**
     * Checks if this application instance has completed.
     * <p>
     * An application is considered completed when its barrier is open.
     * This typically happens when the application is shut down.
     *
     * @return true if the application has completed, false otherwise
     */
    public boolean isCompleted() {
        return barrier.isOpen();
    }

    /**
     * Checks if this application instance is waiting for completion.
     * <p>
     * An application is considered to be waiting for completion when its barrier is closed.
     * This is the normal state of a running application.
     *
     * @return true if the application is waiting for completion, false otherwise
     */
    public boolean isWaitingForCompletion() {
        return barrier.isClosed();
    }

    /**
     * Checks if this application instance is running.
     * <p>
     * An application is considered running after its startup routine has completed
     * and before it is shut down.
     *
     * @return true if the application is running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Checks if this application instance is not running.
     * <p>
     * This is the opposite of {@link #isRunning()}.
     *
     * @return true if the application is not running, false otherwise
     */
    public boolean isNotRunning() {
        return !isRunning;
    }

    /**
     * Waits for this application instance to complete.
     * <p>
     * This method blocks until the application's barrier is open,
     * which typically happens when the application is shut down.
     */
    public void awaitCompletion() {
        barrier.traverse();
    }

    /**
     * Gets the wire repository used by this application instance.
     *
     * @return the wire repository
     */
    public WireContainer wireContainer() {
        return wireContainer;
    }

    /**
     * Gets the banner used by this application instance.
     *
     * @return the banner
     */
    public Banner banner() {
        return banner;
    }

    /**
     * Gets the environment used by this application instance.
     *
     * @return the environment
     */
    public Environment environment() {
        return environment;
    }

    public List<WireContainerCallback> getContextCallbacks() {
        return contextCallbacks;
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
        public @Nullable ShutdownListener get(@NotNull WireContainer wireContainer, @NotNull TypeIdentifier<ShutdownListener> concreteType) {
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
        public void tearDown(WireContainer origin) {
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
            logger.info(() -> "Detected JVM shutdown. Instructing the WireContainer to shutdown.");
            shutdown();
        }
    });
}
