package com.wiredi.telemetry.trace;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.jetbrains.annotations.NotNull;

public class MicrometerSpanContext implements SpanContext {

    private final Tracer tracer;
    private final MicrometerTracingProperties properties;
    private final SpanPropagatorRegistry registry;

    public MicrometerSpanContext(Tracer tracer, MicrometerTracingProperties properties, SpanPropagatorRegistry registry) {
        this.tracer = tracer;
        this.properties = properties;
        this.registry = registry;
    }

    @Override
    public @NotNull SpanType current() {
        Span span = tracer.currentSpan();
        if (span == null) {
            return SpanType.INVALID;
        }

        return new MicrometerSpan(span, properties.headerName());
    }

    @Override
    public @NotNull SpanType currentOrNew() {
        Span span = tracer.currentSpan();
        if (span == null) {
            span = tracer.nextSpan();
        }

        return new MicrometerSpan(span, properties.headerName());
    }

    @Override
    public @NotNull SpanType of(String headerValue) {
        MicrometerTraceToken token = MicrometerTraceToken.parse(headerValue);
        if (token == null) {
            return SpanType.INVALID;
        }

        Span span = tracer.spanBuilder()
                .setParent(token.createTraceContext(tracer))
                .start();

        return new MicrometerSpan(span, properties.headerName());
    }

    @Override
    public SpanPropagatorRegistry propagatorRegistry() {
        return registry;
    }

    @Override
    public void propagateTo(Object carrier) {

    }
}
