package com.wiredi.runtime;

import com.wiredi.annotations.ManualWireCandidate;
import com.wiredi.runtime.domain.StandardWireConflictResolver;
import com.wiredi.runtime.domain.WireConflictResolver;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.function.Supplier;

@ManualWireCandidate
public class WireRepositoryProperties {

    private static final Key KEY_CONTEXT_CALLBACKS_ENABLED = Key.just("wire-di.wired.conflict-resolver");
    private static final Key KEY_WIRE_CONFLICT_RESOLVER = Key.just("wire-di.wired.conflict-resolver");
    private static final Key KEY_LOAD_EAGER_INSTANCE = Key.just("wire-di.startup.load-eager-instances");
    private static final Key KEY_AWAIT_STATES_ENABLED = Key.just("wire-di.startup.await-states");
    private static final Key KEY_AWAIT_STATES_TIMEOUT = Key.just("wire-di.startup.await-states-timeout");
    private static final Key KEY_CONDITIONAL_ROUND_THRESHOLD = Key.just("wire-di.startup.conditional-rounds-threshold");
    private static final WireConflictResolver DEFAULT_CONFLICT_RESOLVER = StandardWireConflictResolver.DEFAULT;
    private static final boolean DEFAULT_CONTEXT_CALLBACKS_ENABLED = true;
    private static final boolean DEFAULT_LOAD_EAGER_INSTANCE = true;
    private static final boolean DEFAULT_AWAIT_STATES_ENABLED = true;
    private static final int DEFAULT_CONDITIONAL_ROUND_THRESHOLD = 3;
    private static final Duration DEFAULT_AWAIT_STATES_TIMEOUT = Duration.ofSeconds(30);

    private final Value<WireConflictResolver> conflictResolver = Value.empty();
    private final Value<Boolean> contextCallbacksEnabled = Value.empty();
    private final Value<Boolean> loadEagerInstance = Value.empty();
    private final Value<Boolean> awaitStatesEnabled = Value.empty();
    private final Value<Duration> awaitStatesTimeout = Value.empty();
    private final Value<Integer> conditionalRoundThreshold = Value.empty();

    @Nullable
    private final Environment environment;

    public WireRepositoryProperties(@Nullable Environment environment) {
        this.environment = environment;
    }

    public WireRepositoryProperties() {
        this(null);
    }

    public Supplier<WireConflictResolver> conflictResolverSupplier() {
        return this::currentConflictResolver;
    }

    public WireConflictResolver currentConflictResolver() {
        if (conflictResolver.isSet()) {
            return conflictResolver.get();
        } else if (environment != null) {
            WireConflictResolver property = environment.getProperty(KEY_WIRE_CONFLICT_RESOLVER, WireConflictResolver.class, DEFAULT_CONFLICT_RESOLVER);
            conflictResolver.set(property);
            return property;
        } else {
            return DEFAULT_CONFLICT_RESOLVER;
        }
    }

    public Duration awaitStatesTimeout() {
        if (awaitStatesTimeout.isSet()) {
            return awaitStatesTimeout.get();
        } else if (environment != null) {
            Duration property = environment.getProperty(KEY_AWAIT_STATES_TIMEOUT, Duration.class, DEFAULT_AWAIT_STATES_TIMEOUT);
            awaitStatesTimeout.set(property);
            return property;
        } else {
            return DEFAULT_AWAIT_STATES_TIMEOUT;
        }
    }

    public int conditionalRoundThreshold() {
        return conditionalRoundThreshold.getOrSet(() -> {
            if (environment == null) {
                return DEFAULT_CONDITIONAL_ROUND_THRESHOLD;
            } else {
                return environment.getProperty(KEY_CONDITIONAL_ROUND_THRESHOLD, int.class, DEFAULT_CONDITIONAL_ROUND_THRESHOLD);
            }
        });
    }

    public boolean contextCallbacksEnabled() {
        return contextCallbacksEnabled.getOrSet(() -> {
            if (environment == null) {
                return DEFAULT_CONTEXT_CALLBACKS_ENABLED;
            } else {
                return environment.getProperty(KEY_CONTEXT_CALLBACKS_ENABLED, boolean.class, DEFAULT_CONTEXT_CALLBACKS_ENABLED);
            }
        });
    }

    public boolean loadEagerInstance() {
        return loadEagerInstance.getOrSet(() -> {
            if (environment == null) {
                return DEFAULT_LOAD_EAGER_INSTANCE;
            } else {
                return environment.getProperty(KEY_LOAD_EAGER_INSTANCE, boolean.class, DEFAULT_LOAD_EAGER_INSTANCE);
            }
        });
    }

    public boolean awaitStates() {
        return awaitStatesEnabled.getOrSet(() -> {
            if (environment == null) {
                return DEFAULT_AWAIT_STATES_ENABLED;
            } else {
                return environment.getProperty(KEY_AWAIT_STATES_ENABLED, boolean.class, DEFAULT_AWAIT_STATES_ENABLED);
            }
        });
    }

    public WireRepositoryProperties withConflictResolver(@NotNull WireConflictResolver conflictResolver) {
        this.conflictResolver.set(conflictResolver);
        return this;
    }

    public WireRepositoryProperties withContextCallbacksEnabled(boolean enabled) {
        this.contextCallbacksEnabled.set(enabled);
        return this;
    }

    public WireRepositoryProperties withLoadEagerInstance(boolean loadEagerInstance) {
        this.loadEagerInstance.set(loadEagerInstance);
        return this;
    }

    public WireRepositoryProperties withAwaitStatesEnabled(boolean awaitStates) {
        this.awaitStatesEnabled.set(awaitStates);
        return this;
    }

    public WireRepositoryProperties withAwaitStatesTimeout(@NotNull Duration timeout) {
        this.awaitStatesTimeout.set(timeout);
        return this;
    }

    public WireRepositoryProperties withConditionalRoundThreshold(int threshold) {
        this.conditionalRoundThreshold.set(threshold);
        return this;
    }
}
