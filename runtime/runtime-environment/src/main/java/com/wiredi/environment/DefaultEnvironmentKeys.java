package com.wiredi.environment;

import com.wiredi.properties.keys.Key;

public class DefaultEnvironmentKeys {

    public static final Key ACTIVE_PROFILES = Key.just("active.profiles");
    public static final Key ADDITIONAL_PROPERTIES = Key.just("load.additional-properties");
    public static final Key DEFAULT_PROFILE_ON_EMPTY = Key.just("wire-di.environment.default-profile-on-empty");
    public static final Key DEFAULT_PROFILES = Key.just("wire-di.environment.default-profiles");
    public static final Key DEFAULT_PROPERTIES = Key.just("wire-di.default-properties");

}
