package com.wiredi.tracer;

import com.wiredi.annotations.Provider;
import com.wiredi.annotations.stereotypes.DefaultConfiguration;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnEnabled;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;
import io.micrometer.tracing.Tracer;

@DefaultConfiguration
@ConditionalOnEnabled("wiredi.autoconfig.micrometer.tracing")
public class TracerDefaultConfiguration {

    @Provider
    @ConditionalOnMissingBean(type = Tracer.class)
    public Tracer defaultTracer() {
        return Tracer.NOOP;
    }

    @Provider
    @ConditionalOnMissingBean(type = Tracer.class)
    public MessageTracer messageTracer(Tracer tracer) {
        return new MessageTracer(tracer);
    }
}
