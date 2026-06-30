package com.wiredi.telemetry.trace;

import io.micrometer.tracing.Span;

import java.util.List;

public class MicrometerSpan implements SpanType {

    private final Span span;
    private final String headerName;

    public MicrometerSpan(Span span, String headerName) {
        this.span = span;
        this.headerName = headerName;
    }

    @Override
    public void makeActive() {
        span.start();
    }

    @Override
    public void close() {
        span.end();
    }

    @Override
    public Iterable<SpanTransportation> transportations() {
        return List.of(new SpanTransportation(headerName, MicrometerTraceToken.of(span).compile()));
    }
}
