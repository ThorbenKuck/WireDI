package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.annotations.ManualWireCandidate;
import com.github.thorbenkuck.di.properties.TypedProperties;
import org.jetbrains.annotations.NotNull;

@ManualWireCandidate
public final class WiredTypesConfiguration {

    private static final String AUTO_LOAD_KEY = "simple.di.wired.autoload";
    private static final String WIRE_CONFLICT_STRATEGY_KEY = "simple.di.wired.concurrent_definition_strategy";
    private static final TypedProperties globalProperties = TypedProperties.fromClassPathOrEmpty("wire.properties");

    static {
        globalProperties.tryTakeFromEnvironment(AUTO_LOAD_KEY, "true");
        globalProperties.tryTakeFromEnvironment(WIRE_CONFLICT_STRATEGY_KEY, WireConflictStrategy.DEFAULT.toString());
    }

    public static void globallySetWireConflictStrategy(@NotNull final WireConflictResolver strategy) {
        globalProperties.set(WIRE_CONFLICT_STRATEGY_KEY, strategy.name());
    }

    @NotNull
    private WireConflictResolver wireConflictStrategy = WireConflictStrategy.valueOf(globalProperties.get(WIRE_CONFLICT_STRATEGY_KEY));

    public boolean doDiAutoLoad() {
        return globalProperties.getBoolean(AUTO_LOAD_KEY);
    }

    public void setWireConflictStrategy(@NotNull final WireConflictStrategy strategy) {
        wireConflictStrategy = strategy;
    }

    public WireConflictResolver conflictStrategy() {
        return wireConflictStrategy;
    }
}
