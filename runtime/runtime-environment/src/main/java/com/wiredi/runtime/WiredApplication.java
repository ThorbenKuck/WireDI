package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.async.barriers.Barrier;
import com.wiredi.runtime.async.barriers.MutableBarrier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class WiredApplication {
    @NotNull
    private static final Consumer<WireRepository> NO_OP_CONFIGURATION = r -> {
    };
    private static final Logging logger = Logging.getInstance(WiredApplication.class);
    public static void main(String[] args) {
        run();
    }

    public static void run() {
        run(NO_OP_CONFIGURATION);
    }

    public static void run(@NotNull Consumer<WireRepository> configuration) {
        WiredApplicationInstance barrier = start(configuration);
        barrier.awaitCompletion();
    }

    public static WiredApplicationInstance start() {
        return start(NO_OP_CONFIGURATION);
    }

    public static WiredApplicationInstance start(@NotNull Consumer<WireRepository> configuration) {
        WireRepository wireRepository = WireRepository.create();
        MutableBarrier barrier = Barrier.create();

        configuration.accept(wireRepository);
        wireRepository.announce(new WiredApplicationInstance.ShutdownListenerProvider(barrier));
        WiredApplicationInstance wiredApplication = new WiredApplicationInstance(wireRepository, barrier);
        wiredApplication.runStartupRoutine();

        logger.info(() -> "Application setup");
        return wiredApplication ;
    }
}
