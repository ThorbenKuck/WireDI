package com.wiredi.telemetry.trace;

import com.wiredi.annotations.properties.Property;
import com.wiredi.annotations.properties.PropertyBinding;

@PropertyBinding(prefix = "wiredi.micrometer.tracing")
public record MicrometerTracingProperties(
        @Property(defaultValue = "true")
        boolean enabled,
        @Property(defaultValue = "micrometertrace")
        String headerName
) {
}
