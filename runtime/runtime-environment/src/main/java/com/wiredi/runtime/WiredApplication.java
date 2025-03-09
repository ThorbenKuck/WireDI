package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.async.barriers.Barrier;
import com.wiredi.runtime.async.barriers.MutableBarrier;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.properties.PropertyLoader;
import com.wiredi.runtime.resources.ResourceLoader;
import com.wiredi.runtime.types.TypeMapper;
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
        return start(Environment.build(),configuration);
    }

    public static WiredApplicationInstance start(Environment environment, @NotNull Consumer<WireRepository> configuration) {
        WireRepository wireRepository = WireRepository.create(environment);
        MutableBarrier barrier = Barrier.create();

        configuration.accept(wireRepository);
        wireRepository.announce(new WiredApplicationInstance.ShutdownListenerProvider(barrier));
        wireRepository.announce(IdentifiableProvider.singleton(environment, TypeIdentifier.just(Environment.class)));
        wireRepository.announce(IdentifiableProvider.singleton(environment.resourceLoader(), TypeIdentifier.just(ResourceLoader.class)));
        wireRepository.announce(IdentifiableProvider.singleton(environment.propertyLoader(), TypeIdentifier.just(PropertyLoader.class)));
        wireRepository.announce(IdentifiableProvider.singleton(environment.typeMapper(), TypeIdentifier.just(TypeMapper.class)));

        WiredApplicationInstance wiredApplication = new WiredApplicationInstance(wireRepository, barrier);
        wiredApplication.runStartupRoutine();

        logger.info(() -> "Application setup");
        return wiredApplication ;
    }
}
