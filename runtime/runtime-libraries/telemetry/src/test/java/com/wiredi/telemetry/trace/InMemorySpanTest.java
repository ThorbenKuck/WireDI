package com.wiredi.telemetry.trace;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

class InMemorySpanTest {

    @Test
    void testDefaultConstructor() {
        // Arrange

        // Act
        InMemorySpan span = new InMemorySpan();

        // Assert
        assertThat(span.transportations()).isNotNull();
        Iterator<SpanTransportation> iterator = span.transportations().iterator();
        assertThat(iterator.hasNext()).isTrue();
        SpanTransportation transportation = iterator.next();
        assertThat(transportation.name()).isEqualTo("traceid");
        assertThat(transportation.value()).isNotNull().isInstanceOf(String.class);
    }

    @Test
    void testCustomTraceIdConstructor() {
        // Arrange
        String traceId = "custom-trace-id";

        // Act
        InMemorySpan span = new InMemorySpan(traceId);

        // Assert
        assertThat(span.transportations()).isNotNull();
        Iterator<SpanTransportation> iterator = span.transportations().iterator();
        assertThat(iterator.hasNext()).isTrue();
        SpanTransportation transportation = iterator.next();
        assertThat(transportation.name()).isEqualTo("traceid");
        assertThat(transportation.value()).isEqualTo(traceId);
    }

    @Test
    void testMakeActiveAndCurrent() {
        // Arrange
        InMemorySpan span = new InMemorySpan();

        // Act
        span.makeActive();

        // Assert
        assertThat(InMemorySpan.current()).isSameAs(span);

        // Cleanup
        span.close();
    }

    @Test
    void testCloseRemovesFromThreadLocal() {
        // Arrange
        InMemorySpan span = new InMemorySpan();
        span.makeActive();
        assertThat(InMemorySpan.current()).isSameAs(span);

        // Act
        span.close();

        // Assert
        assertThat(InMemorySpan.current()).isNull();
    }

    @Test
    void testMultipleSpansInThreadLocal() {
        // Arrange
        InMemorySpan span1 = new InMemorySpan("span-1");
        InMemorySpan span2 = new InMemorySpan("span-2");

        // Act Assert
        span1.makeActive();
        assertThat(InMemorySpan.current()).isSameAs(span1);
        span2.makeActive();
        assertThat(InMemorySpan.current()).isSameAs(span2);
        span2.close();
        assertThat(InMemorySpan.current()).isNull();
        span1.makeActive();
        assertThat(InMemorySpan.current()).isSameAs(span1);
        span1.close();
        assertThat(InMemorySpan.current()).isNull();
    }
}
