package com.wiredi.runtime.environment.builtin;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.lang.Ordered;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import org.jetbrains.annotations.NotNull;

import static com.wiredi.runtime.environment.DefaultEnvironmentKeys.ACTIVE_PROFILES;
import static com.wiredi.runtime.environment.DefaultEnvironmentKeys.ACTIVE_PROFILES_VALUE;

/**
 * This environment configuration takes active profiles from the system and environment.
 * <p>
 * By having this, the developers can specify the active profiles by either environment or system variables.
 */
@AutoService(EnvironmentConfiguration.class)
public class EarlyProfileDetermination implements EnvironmentConfiguration {

    public static final int ORDER = Ordered.before(ProfilePropertiesEnvironmentConfiguration.ORDER);

    @Override
    public void configure(@NotNull Environment environment) {
        tryAddActiveProfiles(environment, System.getProperty(ACTIVE_PROFILES_VALUE));
        tryAddActiveProfiles(environment, environment.getProperty(ACTIVE_PROFILES));
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    private void tryAddActiveProfiles(Environment environment, String properties) {
        if (properties == null) {
            return;
        }

        environment.properties()
                .splitKey(properties)
                .forEach(environment::addActiveProfile);
    }
}
