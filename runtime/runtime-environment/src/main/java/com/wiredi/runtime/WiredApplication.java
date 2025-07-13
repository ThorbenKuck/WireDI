package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.async.barriers.Barrier;
import com.wiredi.runtime.async.barriers.MutableBarrier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * The main entry point for WireDI applications.
 * <p>
 * This class provides static methods for starting and running a wired application,
 * abstracting away the complexity of setting up and configuring a {@link WireContainer}.
 * <p>
 * There are two main ways to use this class:
 * <ul>
 *   <li>Use the {@link #run()} or {@link #run(Consumer)} methods to start an application
 *       and block until it completes.</li>
 *   <li>Use the {@link #start()} or {@link #start(Consumer)} methods to start an application
 *       and get a {@link WiredApplicationInstance} that can be used to control the application.</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * // Start and run an application with default configuration
 * WiredApplication.run();
 *
 * // Start and run an application with custom configuration
 * WiredApplication.run(wireRepository -> {
 *     // Configure the wireRepository
 *     wireRepository.announce(new CustomComponent());
 * });
 *
 * // Start an application with custom configuration and get the instance
 * WiredApplicationInstance instance = WiredApplication.start(wireRepository -> {
 *     // Configure the wireRepository
 *     wireRepository.announce(new CustomComponent());
 * });
 * }</pre>
 *
 * @see WireContainer
 * @see WiredApplicationInstance
 * @see Environment
 */
public class WiredApplication {
    @NotNull
    private static final Consumer<WireContainer> NO_OP_CONFIGURATION = r -> {
    };
    private static final Logging logger = Logging.getInstance(WiredApplication.class);

    /**
     * Main entry point for running a WireDI application from the command line.
     * <p>
     * This method starts a WireDI application with default configuration and blocks until it completes.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        run();
    }

    /**
     * Starts a WireDI application with default configuration and blocks until it completes.
     * <p>
     * This is a convenience method for {@link #run(Consumer)} with no configuration.
     */
    public static void run() {
        run(NO_OP_CONFIGURATION);
    }

    /**
     * Starts a WireDI application with the specified configuration and blocks until it completes.
     * <p>
     * This method creates a new {@link WiredApplicationInstance}, applies the specified configuration,
     * and then calls {@link WiredApplicationInstance#awaitCompletion()} to block until the application completes.
     *
     * @param configuration a consumer that configures the {@link WireContainer}
     */
    public static void run(@NotNull Consumer<WireContainer> configuration) {
        WiredApplicationInstance barrier = start(configuration);
        barrier.awaitCompletion();
    }

    /**
     * Starts a WireDI application with default configuration and returns the application instance.
     * <p>
     * This is a convenience method for {@link #start(Consumer)} with no configuration.
     *
     * @return the created {@link WiredApplicationInstance}
     */
    public static WiredApplicationInstance start() {
        return start(NO_OP_CONFIGURATION);
    }

    /**
     * Starts a WireDI application with the specified configuration and returns the application instance.
     * <p>
     * This is a convenience method for {@link #start(Environment, Consumer)} that creates a default environment.
     *
     * @param configuration a consumer that configures the {@link WireContainer}
     * @return the created {@link WiredApplicationInstance}
     */
    public static WiredApplicationInstance start(@NotNull Consumer<WireContainer> configuration) {
        return start(Environment.build(), configuration);
    }

    /**
     * Starts a WireDI application with the specified environment and configuration and returns the application instance.
     * <p>
     * This method creates a new {@link WireContainer} with the specified environment, applies the specified configuration,
     * and then creates and returns a new {@link WiredApplicationInstance}.
     *
     * @param environment   the environment to use for the application
     * @param configuration a consumer that configures the {@link WireContainer}
     * @return the created {@link WiredApplicationInstance}
     */
    public static WiredApplicationInstance start(Environment environment, @NotNull Consumer<WireContainer> configuration) {
        WireContainer wireRepository = WireContainer.create(environment);
        MutableBarrier barrier = Barrier.create();

        configuration.accept(wireRepository);
        wireRepository.announce(new WiredApplicationInstance.ShutdownListenerProvider(barrier));

        WiredApplicationInstance wiredApplication = new WiredApplicationInstance(wireRepository, barrier);
        wiredApplication.start();

        logger.info(() -> "Application setup");
        return wiredApplication;
    }
}
