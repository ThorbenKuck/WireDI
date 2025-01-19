package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.async.barriers.Barrier;
import com.wiredi.runtime.async.barriers.MuitableBarrier;
import com.wiredi.runtime.domain.Disposable;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public record Application(WireRepository wireRepository, Barrier barrier) {

    private static final Logging logger = Logging.getInstance(Application.class);

    public boolean isActive() {
        return barrier.isClosed();
    }

    public void awaitCompletion() {
        barrier.traverse();
    }

    public void stop() {
        wireRepository.destroy();
    }

    @NotNull
    private static final Consumer<WireRepository> NO_OP_CONFIGURATION = r -> {
    };

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        run(NO_OP_CONFIGURATION);
    }

    public static void run(@NotNull Consumer<WireRepository> configuration) {
        Application barrier = start(configuration);
        barrier.awaitCompletion();
    }

    public static Application start() {
        return start(NO_OP_CONFIGURATION);
    }

    public static Application start(@NotNull Consumer<WireRepository> configuration) {
        WireRepository wireRepository = WireRepository.create();
        MuitableBarrier barrier = Barrier.create();

        configuration.accept(wireRepository);
        wireRepository.announce(new ShutdownListenerProvider(barrier));
        wireRepository.load();

        logger.info(() -> "Application setup");
        return new Application(wireRepository, barrier);
    }

    static class ShutdownListenerProvider implements IdentifiableProvider<ShutdownListener> {

        private final ShutdownListener instance;
        private static final TypeIdentifier<ShutdownListener> TYPE = TypeIdentifier.of(ShutdownListener.class);
        private static final List<TypeIdentifier<?>> ADDITIONAL_TYPES = List.of(TypeIdentifier.of(Disposable.class));

        ShutdownListenerProvider(MuitableBarrier barrier) {
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

        private final MuitableBarrier barrier;

        ShutdownListener(MuitableBarrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void tearDown(WireRepository origin) {
            logger.info(() -> "Application shutdown detected");
            barrier.open();
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
}
