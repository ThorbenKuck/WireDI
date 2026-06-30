package com.wiredi.telemetry.trace;

import java.util.List;
import java.util.UUID;

public class InMemorySpan implements SpanType {

    private static final ThreadLocal<InMemorySpan> currentSpan = new ThreadLocal<>();
    private final String traceId;
    private final List<SpanTransportation> transportations;

    public InMemorySpan() {
        this(UUID.randomUUID().toString().replace("-", ""));
    }

    public InMemorySpan(String traceId) {
        this.traceId = traceId;
        this.transportations = List.of(new SpanTransportation("traceid", traceId));
    }

    protected static InMemorySpan current() {
        return currentSpan.get();
    }

    @Override
    public void makeActive() {
        currentSpan.set(this);
    }

    @Override
    public Iterable<SpanTransportation> transportations() {
        return transportations;
    }

    @Override
    public void close() {
        currentSpan.remove();
    }
}
