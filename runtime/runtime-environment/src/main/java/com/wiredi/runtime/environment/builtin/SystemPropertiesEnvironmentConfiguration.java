package com.wiredi.runtime.environment.builtin;

import com.google.auto.service.AutoService;
import com.wiredi.logging.Logging;
import com.wiredi.runtime.lang.Ordered;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.environment.DefaultEnvironmentKeys;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import org.jetbrains.annotations.NotNull;

@AutoService(EnvironmentConfiguration.class)
public class SystemPropertiesEnvironmentConfiguration implements EnvironmentConfiguration {

    public static final int ORDER = Ordered.after(OSEnvironmentConfiguration.ORDER);
    private static final Logging logger = Logging.getInstance(SystemPropertiesEnvironmentConfiguration.class);

    @Override
    public void configure(@NotNull Environment environment) {
        if (environment.getProperty(DefaultEnvironmentKeys.AUTO_LOAD_SYSTEM_PROPERTIES, Boolean.class, true)) {
            logger.debug(() -> "Loading system variables");
            environment.setProperties(System.getProperties());
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
