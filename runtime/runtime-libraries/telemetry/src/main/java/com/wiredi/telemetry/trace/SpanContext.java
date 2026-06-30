package com.wiredi.telemetry.trace;

import org.jetbrains.annotations.NotNull;

public interface SpanContext {

    static SpanContext inMemory() {
        return new InMemorySpanContext();
    }

    /**
     * Returns the current span.
     * <p>
     * If no span is active, returns {@link SpanType#INVALID}.
     *
     * @return the current span
     */
    @NotNull
    SpanType current();

    /**
     * Returns the current span or creates a new one if none is active.
     *
     * @return the current span or a new one
     */
    @NotNull
    SpanType currentOrNew();

    /**
     * Parses the given header value and returns a new SpanType for it.
     *
     * @param headerValue the header value to parse.
     * @return a new SpanType for the given header value or {@link SpanType#INVALID} if the header value is invalid.
     */
    @NotNull
    SpanType of(String headerValue);

    SpanPropagatorRegistry propagatorRegistry();

    void propagateTo(Object carrier);

}
