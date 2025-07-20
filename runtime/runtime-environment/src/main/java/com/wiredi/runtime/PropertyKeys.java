package com.wiredi.runtime;

import com.wiredi.runtime.domain.StandardWireConflictResolver;
import com.wiredi.runtime.properties.Key;

/**
 * This enum defines standard property keys used throughout the WireDI framework.
 * <p>
 * These constants provide a centralized definition of property keys that control
 * various aspects of the WireContainer and WiredApplication behavior, such as
 * conflict resolution, conditional processing, and startup configuration.
 * <p>
 * Each enum constant includes both the property key string and a default value
 * that will be used if the property is not explicitly set.
 *
 * @see com.wiredi.runtime.Environment
 * @see com.wiredi.runtime.properties.Key
 * @see com.wiredi.runtime.WireContainer
 */
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
    DEBUG("debug", false),
    ;

    private final Key rawKey;
    private final Object defaultValue;

    /**
     * Constructs a new PropertyKeys enum constant.
     *
     * @param rawKey the string representation of the property key
     * @param defaultValue the default value to use if the property is not set
     */
    PropertyKeys(String rawKey, Object defaultValue) {
        this.rawKey = Key.just(rawKey);
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the Key object representing this property key.
     * <p>
     * This Key can be used with the Environment to get or set property values.
     *
     * @return the Key object for this property
     * @see com.wiredi.runtime.Environment#getProperty(Key)
     */
    public Key getKey() {
        return rawKey;
    }

    /**
     * Returns the default value for this property.
     * <p>
     * This value is used when the property is not explicitly set in the environment.
     *
     * @return the default value for this property
     */
    public Object getDefaultValue() {
        return defaultValue;
    }
}
