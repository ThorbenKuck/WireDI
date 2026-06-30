package com.wiredi.telemetry.metrics;

import com.wiredi.annotations.Provider;
import com.wiredi.annotations.stereotypes.DefaultConfiguration;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnEnabled;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.util.List;

@DefaultConfiguration
@ConditionalOnEnabled("wiredi.autoconfig.micrometer.metrics")
public class MetricCollectorDefaultConfiguration {

    @Provider
    @ConditionalOnMissingBean(SimpleMeterRegistry.class)
    public SimpleMeterRegistry defaultMeterRegistry(Environment environment, MicrometerMetricsProperties properties, Clock clock) {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry(new EnvironmentSimpleConfig(environment, properties), clock);
        io.micrometer.core.instrument.Metrics.addRegistry(meterRegistry);
        return meterRegistry;
    }

    @Provider
    @ConditionalOnMissingBean(com.wiredi.telemetry.metrics.Metrics.class)
    public com.wiredi.telemetry.metrics.Metrics metrics(Clock clock, List<MeterRegistry> meterRegistry) {
        if (meterRegistry.isEmpty()) {
            return Metrics.NOOP;
        } else if (meterRegistry.size() > 1) {
            return new MicrometerMetrics(new CompositeMeterRegistry(clock, meterRegistry));
        }

        return new MicrometerMetrics(meterRegistry.getFirst());
    }

    @Provider
    @ConditionalOnMissingBean(Clock.class)
    public Clock clock() {
        return Clock.SYSTEM;
    }
}
