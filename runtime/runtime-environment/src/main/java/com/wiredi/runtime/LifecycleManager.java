package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.async.StateFull;
import com.wiredi.runtime.domain.Eager;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.time.Timed;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;

public interface LifecycleManager {

    void preInitialization(WireContainer container);

    void postInitialization(WireContainer container, WireContainer.LoadConfig loadConfig);

    class Default implements LifecycleManager {

        private static final Logging logger = Logging.getInstance(Default.class);

        @Override
        public void preInitialization(WireContainer container) {

        }

        @Override
        public void postInitialization(WireContainer container, WireContainer.LoadConfig loadConfig) {
            if (loadConfig.initializeEagerBeans()) {
                container.startupDiagnostics().measure("WireContainer.initializeEagerBeans", () -> initializeEagerBeans(container));
            }

            if (loadConfig.synchronizeOnStates()) {
                container.startupDiagnostics().measure("WireContainer.synchronizeOnStates", () -> synchronizeOnStates(container, loadConfig.stateFullMaxTimeout()));
            }

        }

        private Timed initializeEagerBeans(WireContainer container) {
            return Timed.of(() -> {
                logger.trace(() -> "Checking for eager classes");
                final Collection<Eager> eagerInstances = container.getAll(Eager.class);
                if (!eagerInstances.isEmpty()) {
                    final EagerInitializer initializer = container.tryGet(EagerInitializer.class).orElse(new EagerInitializer.ParallelStream());
                    logger.debug(() -> "Loading " + eagerInstances.size() + " eager classes.");
                    initializer.initialize(container, eagerInstances);
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
        private Timed synchronizeOnStates(WireContainer container, @Nullable Duration timeout) {
            return Timed.of(() -> {
                logger.trace(() -> "Synchronizing in states");
                // Writing StateFull<?> right here leads to compile time errors, this
                // is why we explicitly skip the raw type inspection with the following comment
                final Collection<StateFull<?>> stateFulls = container.getAll(TypeIdentifier.just(StateFull.class).cast());
                final StateFullInitializer stateFullInitializer = container.tryGet(StateFullInitializer.class).orElse(new StateFullInitializer.ParallelStream());
                if (!stateFulls.isEmpty()) {
                    logger.debug(() -> "Synchronizing on " + stateFulls.size() + " StateFull instances.");
                    stateFullInitializer.initialize(container, stateFulls, timeout);
                }
            });
        }
    }
}
