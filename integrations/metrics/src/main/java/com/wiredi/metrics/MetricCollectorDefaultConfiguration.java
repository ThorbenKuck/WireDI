package com.wiredi.metrics;

import com.wiredi.annotations.Provider;
import com.wiredi.annotations.stereotypes.DefaultConfiguration;
import com.wiredi.runtime.Environment;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnEnabled;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@DefaultConfiguration
@ConditionalOnEnabled("wiredi.autoconfig.micrometer.metrics")
public class MetricCollectorDefaultConfiguration {

    @Provider
    @ConditionalOnMissingBean
    public EnvironmentSimpleConfig environmentSimpleConfig(Environment environment, MicrometerMetricsProperties properties) {
        return new EnvironmentSimpleConfig(environment, properties);
    }

    @Provider
    @ConditionalOnMissingBean(MeterRegistry.class)
    public MeterRegistry defaultMeterRegistry(SimpleConfig simpleConfig, Clock clock) {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry(simpleConfig, clock);
        Metrics.addRegistry(meterRegistry);
        return meterRegistry;
    }

    @Provider
    @ConditionalOnMissingBean(Clock.class)
    public Clock clock() {
        return Clock.SYSTEM;
    }
}
