package com.wiredi.runtime.environment.builtin;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.domain.Ordered;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Collectors;

@AutoService(EnvironmentConfiguration.class)
public class OSEnvironmentConfiguration implements EnvironmentConfiguration {

    public static final int ORDER = Ordered.after(ProfilePropertiesEnvironmentConfiguration.ORDER);

    public static final Value<Map<Key, String>> ENVIRONMENT_VARIABLES = Value.async(() -> System.getenv()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(it -> Key.format(it.getKey()).compile(), Map.Entry::getValue))
    );

    @Override
    public void configure(@NotNull Environment environment) {
        environment.properties().setAll(ENVIRONMENT_VARIABLES.get());
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
