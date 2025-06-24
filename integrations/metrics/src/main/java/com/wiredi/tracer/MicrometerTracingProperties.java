package com.wiredi.tracer;

import com.wiredi.annotations.properties.Property;
import com.wiredi.annotations.properties.PropertyBinding;

@PropertyBinding(prefix = "wiredi.micrometer.tracing")
public record MicrometerTracingProperties(
        @Property(defaultValue = "true")
        boolean enabled
) {
}
