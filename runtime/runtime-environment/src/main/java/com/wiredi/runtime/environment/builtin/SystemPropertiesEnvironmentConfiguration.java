package com.wiredi.runtime.environment.builtin;

import com.google.auto.service.AutoService;
import com.wiredi.runtime.domain.Ordered;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import org.jetbrains.annotations.NotNull;

@AutoService(EnvironmentConfiguration.class)
public class SystemPropertiesEnvironmentConfiguration implements EnvironmentConfiguration {

    public static final int ORDER = Ordered.after(OSEnvironmentConfiguration.ORDER);

    @Override
    public void configure(@NotNull Environment environment) {
        environment.properties().setAll(System.getProperties());
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
