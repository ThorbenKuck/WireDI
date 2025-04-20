package com.wiredi.runtime.environment.builtin;

import com.google.auto.service.AutoService;
import com.wiredi.logging.Logging;
import com.wiredi.runtime.lang.Ordered;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Collectors;

import static com.wiredi.runtime.environment.DefaultEnvironmentKeys.AUTO_LOAD_ENVIRONMENT_PROPERTIES;

@AutoService(EnvironmentConfiguration.class)
public class OSEnvironmentConfiguration implements EnvironmentConfiguration {

    public static final int ORDER = Ordered.after(ProfilePropertiesEnvironmentConfiguration.ORDER);
    private static final Logging logger = Logging.getInstance(OSEnvironmentConfiguration.class);

    public static final Value<Map<Key, String>> ENVIRONMENT_VARIABLES = Value.async(() -> System.getenv()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(it -> Key.format(it.getKey()).compile(), Map.Entry::getValue))
    );

    @Override
    public void configure(@NotNull Environment environment) {
        if (environment.getProperty(AUTO_LOAD_ENVIRONMENT_PROPERTIES, Boolean.class, true)) {
            logger.debug(() -> "Loading environment variables");
            environment.setProperties(ENVIRONMENT_VARIABLES.get());
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
