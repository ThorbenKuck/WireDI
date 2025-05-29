package com.wiredi.runtime;

import com.wiredi.runtime.domain.StandardWireConflictResolver;
import com.wiredi.runtime.properties.Key;

public enum PropertyKeys {

    /**
     * Property to point to the conflict resolver of the BeanContainer
     */
    WIRE_CONFLICT_RESOLVER("wiredi.wired.conflict-resolver", StandardWireConflictResolver.DEFAULT.name()),

    /**
     * How many rounds the BeanContainer should attempt to check and apply conditions in IdentifiableProviders
     */
    CONDITIONAL_ROUND_THRESHOLD("wiredi.conditional-rounds-threshold", 10),
    /**
     * Whether the WiredApplication should attempt to load the eager instances or not
     */
    LOAD_EAGER_INSTANCES("wiredi.startup.load-eager-instances", true),
    /**
     * Whether the WiredApplication should wait until all states are initialized
     */
    AWAIT_STATES("wiredi.startup.await-states", true),
    /**
     * How long the WiredApplication should wait until all states are loaded.
     * <p>
     * If this property is absent, in other words, null, the WiredApplication is waiting endlessly.
     */
    AWAIT_STATES_TIMEOUT("wiredi.startup.await-states-timeout", null),
    PRINT_DIAGNOSTICS("wiredi.startup.print-diagnostics", false),
    ;

    private final Key rawKey;
    private final Object defaultValue;

    PropertyKeys(String rawKey, Object defaultValue) {
        this.rawKey = Key.just(rawKey);
        this.defaultValue = defaultValue;
    }

    public Key getKey() {
        return rawKey;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
