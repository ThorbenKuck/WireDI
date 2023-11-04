package com.wiredi.runtime;

import com.wiredi.annotations.ManualWireCandidate;
import com.wiredi.domain.StandardWireConflictResolver;
import com.wiredi.domain.WireConflictResolver;
import com.wiredi.environment.Environment;
import com.wiredi.lang.values.NeverNullValue;
import com.wiredi.properties.TypedProperties;
import com.wiredi.properties.keys.Key;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

@ManualWireCandidate
public class WiredRepositoryProperties {

    private static final Key WIRE_CONFLICT_RESOLVER_KEY = Key.just("wire-di.wired.conflict-resolver");
    private static final Key CONTEXT_CALLBACKS_KEY = Key.just("wire-di.startup.context-callbacks-enabled");
    private static final Key LOAD_EAGER_INSTANCE = Key.just("wire-di.startup.load-eager-instances");
    private static final Key AWAIT_STATES = Key.just("wire-di.startup.await-states");
    private static final Key AWAIT_STATES_TIMEOUT = Key.just("wire-di.startup.await-states-timeout");
    private final NeverNullValue<WireConflictResolver> conflictResolver = new NeverNullValue<>(StandardWireConflictResolver.DEFAULT);
    private final NeverNullValue<Boolean> contextCallbacks = new NeverNullValue<>(true);
    private final NeverNullValue<Boolean> loadEagerInstance = new NeverNullValue<>(true);
    private final NeverNullValue<Boolean> awaitStates = new NeverNullValue<>(true);
    private final NeverNullValue<Duration> awaitStatesTimeout = new NeverNullValue<>(Duration.of(30, ChronoUnit.SECONDS));

    public void load(Environment environment) {
        TypedProperties properties = environment.properties();

        contextCallbacks.trySet(properties.getBoolean(CONTEXT_CALLBACKS_KEY, contextCallbacks.get()));
        loadEagerInstance.trySet(properties.getBoolean(LOAD_EAGER_INSTANCE, loadEagerInstance.get()));
        awaitStates.trySet(properties.getBoolean(AWAIT_STATES, awaitStates.get()));
        environment.map(AWAIT_STATES_TIMEOUT, Duration::parse).ifPresent(awaitStatesTimeout::set);
        environment.map(WIRE_CONFLICT_RESOLVER_KEY, StandardWireConflictResolver::determine).ifPresent(conflictResolver::set);
    }

    public WireConflictResolver currentConflictResolver() {
        return conflictResolver.get();
    }

    public Supplier<WireConflictResolver> conflictResolverSupplier() {
        return conflictResolver.asSupplier();
    }

    public boolean contextCallbacksEnabled() {
        return contextCallbacks.get();
    }

    public Duration awaitStatesTimeout() {
        return awaitStatesTimeout.get();
    }

    public boolean loadEagerInstance() {
        return loadEagerInstance.get();
    }

    public boolean awaitStates() {
        return awaitStates.get();
    }

    public void setConflictResolver(@NotNull WireConflictResolver conflictResolver) {
        this.conflictResolver.set(conflictResolver);
    }

    public void setAwaitStatesTimeout(Duration timeout) {
        this.awaitStatesTimeout.set(timeout);
    }

    public void setContextCallbacksEnabled(boolean enabled) {
        this.contextCallbacks.set(enabled);
    }

    public void setLoadEagerInstance(boolean loadEagerInstance) {
        this.loadEagerInstance.set(loadEagerInstance);
    }

    public void setAwaitStates(boolean awaitStates) {
        this.awaitStates.set(awaitStates);
    }
}
