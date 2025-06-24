package com.wiredi.metrics;

import com.wiredi.annotations.properties.Description;
import com.wiredi.annotations.properties.Property;
import com.wiredi.annotations.properties.PropertyBinding;
import io.micrometer.core.instrument.simple.CountingMode;

import java.time.Duration;

@PropertyBinding(prefix = "wiredi.micrometer.metrics")
public record MicrometerMetricsProperties(
        @Property(defaultValue = "simple")
        @Description("The prefix for the MeterRegistry")
        String prefix,
        @Property(defaultValue = "PT1M")
        Duration step,
        @Property(defaultValue = "CUMULATIVE")
        CountingMode mode,
        @Property(defaultValue = "true")
        boolean enabled
) {
}
