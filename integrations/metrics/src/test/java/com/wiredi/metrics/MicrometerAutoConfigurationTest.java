package com.wiredi.metrics;

import com.wiredi.runtime.WireContainer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MicrometerAutoConfigurationTest {

    @Test
    void shouldCreateDefaultMeterRegistry() {
        // Arrange
        WireContainer repository = WireContainer.open();

        // Act
        MeterRegistry meterRegistry = repository.get(MeterRegistry.class);

        // Assert
        assertNotNull(meterRegistry);
        assertTrue(meterRegistry instanceof SimpleMeterRegistry);
    }

    @Test
    void shouldCreateMetricCollector() {
        // Arrange
        WireContainer repository = WireContainer.open();

        // Act
        MeterRegistry metricCollector = repository.get(MeterRegistry.class);

        // Assert
        assertNotNull(metricCollector);
    }
}