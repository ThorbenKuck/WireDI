package com.wiredi.runtime.environment;

import com.wiredi.runtime.properties.Key;

/**
 * This class defines standard environment property keys used throughout the WireDI framework.
 * <p>
 * These constants provide a centralized definition of property keys that control
 * various aspects of the environment configuration, such as active profiles,
 * additional properties files, and default settings.
 * <p>
 * Using these constants instead of hardcoded strings helps maintain consistency
 * and makes it easier to track and update property keys across the codebase.
 *
 * @see com.wiredi.runtime.Environment
 * @see com.wiredi.runtime.properties.Key
 */
public class DefaultEnvironmentKeys {

    public static final String ACTIVE_PROFILES_VALUE = "active.profiles";
    public static final Key ACTIVE_PROFILES = Key.just(ACTIVE_PROFILES_VALUE);

    public static final String ADDITIONAL_PROPERTIES_VALUE = "com.wiredi.environment.additional-properties";
    public static final Key ADDITIONAL_PROPERTIES = Key.just(ADDITIONAL_PROPERTIES_VALUE);

    public static final String APPLY_TYPE_CONVERTERS_VALUE = "com.wiredi.environment.apply-type-converters";
    public static final Key APPLY_TYPE_CONVERTERS = Key.just(APPLY_TYPE_CONVERTERS_VALUE);

    public static final String DEFAULT_PROFILE_VALUE = "com.wiredi.environment.default-profile";
    public static final Key DEFAULT_PROFILE = Key.just(DEFAULT_PROFILE_VALUE);

    public static final String AUTO_LOAD_ENVIRONMENT_PROPERTIES_VALUE = "com.wiredi.environment.auto-load.environment";
    public static final Key AUTO_LOAD_ENVIRONMENT_PROPERTIES = Key.just(AUTO_LOAD_ENVIRONMENT_PROPERTIES_VALUE);

    public static final String AUTO_LOAD_SYSTEM_PROPERTIES_VALUE = "com.wiredi.environment.auto-load.system";
    public static final Key AUTO_LOAD_SYSTEM_PROPERTIES = Key.just(AUTO_LOAD_SYSTEM_PROPERTIES_VALUE);

    public static final String DEFAULT_PROPERTIES_VALUE = "com.wiredi.environment.default-properties";
    public static final Key DEFAULT_PROPERTIES = Key.just(DEFAULT_PROPERTIES_VALUE);

}
