package com.wiredi.metrics;

import com.wiredi.annotations.Provider;
import com.wiredi.annotations.stereotypes.AutoConfiguration;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnProperty;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.util.List;

@AutoConfiguration
@ConditionalOnProperty(
        key = "wiredi.micrometer.autoconfigure",
        havingValue = "true",
        matchIfMissing = true
)
public class MicrometerAutoConfiguration {

    @Provider
    @ConditionalOnProperty(
            key = "wiredi.metrics.connect-metric-awares",
            havingValue = "true",
            matchIfMissing = true
    )
    public MetricAwareConnectorBridge metricAwareConnectorBridge() {
        return new MetricAwareConnectorBridge();
    }

    @Provider
    @ConditionalOnMissingBean(type = MeterRegistry.class)
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Provider
    @ConditionalOnMissingBean(type = MetricCollector.class)
    public MetricCollector metricCollector(MeterRegistry meterRegistry, List<MetricAware> metricAwares) {
        MicrometerMetricCollector micrometerMetricCollector = new MicrometerMetricCollector(meterRegistry);
        metricAwares.forEach(metricAware -> metricAware.setMetricCollector(micrometerMetricCollector));
        return micrometerMetricCollector;
    }
}
