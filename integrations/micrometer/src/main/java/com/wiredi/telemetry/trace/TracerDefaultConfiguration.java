package com.wiredi.telemetry.trace;

import com.wiredi.annotations.Provider;
import com.wiredi.annotations.stereotypes.DefaultConfiguration;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnEnabled;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;
import io.micrometer.tracing.Tracer;

import java.util.List;

@DefaultConfiguration
@ConditionalOnEnabled("wiredi.autoconfig.micrometer.tracing")
public class TracerDefaultConfiguration {

    @Provider
    @ConditionalOnMissingBean(type = Tracer.class)
    public Tracer defaultTracer() {
        return Tracer.NOOP;
    }

    @Provider
    @ConditionalOnMissingBean(SpanPropagatorRegistry.class)
    public SpanPropagatorRegistry propagatorRegistry(List<SpanPropagator> propagators) {
        return new SpanPropagatorRegistry(propagators);
    }

    @Provider
    @ConditionalOnMissingBean(type = SpanContext.class)
    public SpanContext messageTracer(Tracer tracer, MicrometerTracingProperties properties, SpanPropagatorRegistry registry) {
        if (tracer == Tracer.NOOP) {
            return new InMemorySpanContext();
        }
        return new MicrometerSpanContext(tracer, properties, registry);
    }
}
