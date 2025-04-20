package com.wiredi.metrics;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.exceptions.BeanNotFoundException;
import com.wiredi.runtime.properties.Key;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MicrometerAutoConfigurationTest {

    @Test
    void shouldCreateDefaultMeterRegistry() {
        // Arrange
        WireRepository repository = WireRepository.open();

        // Act
        MeterRegistry meterRegistry = repository.get(MeterRegistry.class);

        // Assert
        assertNotNull(meterRegistry);
        assertTrue(meterRegistry instanceof SimpleMeterRegistry);
    }

    @Test
    void shouldCreateMetricCollector() {
        // Arrange
        WireRepository repository = WireRepository.open();

        // Act
        MetricCollector metricCollector = repository.get(MetricCollector.class);

        // Assert
        assertNotNull(metricCollector);
        assertTrue(metricCollector instanceof MicrometerMetricCollector);
    }

    @Test
    void shouldNotCreateMetricAwareBridgeWhenDisabled() {
        // Arrange
        WireRepository repository = WireRepository.create();
        repository.environment().setProperty(Key.just("wiredi.metrics.connect-metric-awares"), "false");
        repository.load();

        // Act/Assert
        BeanNotFoundException beanNotFoundException = assertThrows(BeanNotFoundException.class, () ->
                repository.get(MetricAwareConnectorBridge.class)
        );
        assertEquals("Could not find a bean for the type com.wiredi.metrics.MetricAwareConnectorBridge", beanNotFoundException.getMessage());
    }

    @Test
    void shouldCreateMetricAwareBridgeByDefault() {
        // Arrange
        WireRepository repository = WireRepository.open();

        // Act
        MetricAwareConnectorBridge bridge = repository.get(MetricAwareConnectorBridge.class);

        // Assert
        assertNotNull(bridge);
    }

    @Test
    void shouldConnectMetricAwareComponents() {
        // Arrange
        TestMetricAware testMetricAware = new TestMetricAware();
        WireRepository repository = WireRepository.create();
        repository.announce(IdentifiableProvider.singleton(testMetricAware)
                .withAdditionalTypeIdentifier(TypeIdentifier.just(MetricAware.class))
        );
        repository.load();

        // Act
        MetricCollector metricCollector = repository.get(MetricCollector.class);

        // Assert
        assertNotNull(testMetricAware.getMetricCollector());
        assertSame(metricCollector, testMetricAware.getMetricCollector());
    }

    private static class TestMetricAware implements MetricAware {
        private MetricCollector metricCollector;

        @Override
        public void setMetricCollector(MetricCollector metricCollector) {
            this.metricCollector = metricCollector;
        }

        public MetricCollector getMetricCollector() {
            return metricCollector;
        }
    }
}