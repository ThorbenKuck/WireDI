package com.wiredi.metrics;

import com.wiredi.runtime.Environment;
import com.wiredi.runtime.properties.Key;
import io.micrometer.core.instrument.simple.CountingMode;
import io.micrometer.core.instrument.simple.SimpleConfig;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class EnvironmentSimpleConfig implements SimpleConfig {

    @NotNull
    private final Environment environment;
    @NotNull
    private final MicrometerMetricsProperties properties;

    public EnvironmentSimpleConfig(
            @NotNull Environment environment,
            @NotNull MicrometerMetricsProperties properties
    ) {
        this.environment = environment;
        this.properties = properties;
    }

    @Override
    public String get(@NotNull String key) {
        return environment.getProperty(Key.format(key));
    }

    @Override
    public @NotNull String prefix() {
        return properties.prefix();
    }

    @Override
    public @NotNull Duration step() {
        return properties.step();
    }

    @Override
    public @NotNull CountingMode mode() {
        return properties.mode();
    }
}
