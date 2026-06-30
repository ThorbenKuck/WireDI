package com.wiredi.telemetry.trace;

import org.jetbrains.annotations.NotNull;

public class InMemorySpanContext implements SpanContext {

    @Override
    public @NotNull SpanType current() {
        return InMemorySpan.current();
    }

    @Override
    public @NotNull SpanType currentOrNew() {
        InMemorySpan span = InMemorySpan.current();
        if (span != null) {
            return span;
        }

        InMemorySpan newSpan = new InMemorySpan();
        newSpan.makeActive();
        return newSpan;
    }

    @Override
    public @NotNull SpanType of(String headerValue) {
        return null;
    }

    @Override
    public SpanPropagatorRegistry propagatorRegistry() {
        return null;
    }

    @Override
    public void propagateTo(Object carrier) {

    }
}
