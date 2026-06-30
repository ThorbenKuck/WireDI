package com.wiredi.telemetry.trace;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SpanPropagatorRegistryTest {

    @Test
    void testPropagateSuccessWithSinglePropagator() {
        // Arrange
        SpanPropagator propagator = mock(SpanPropagator.class);
        SpanPropagatorRegistry registry = new SpanPropagatorRegistry(List.of(propagator));
        SpanTransportation transportation = new SpanTransportation("test", "value");
        Object carrier = new Object();
        
        when(propagator.propagate(transportation, carrier)).thenReturn(true);

        // Act
        boolean result = registry.propagate(List.of(transportation), carrier);

        // Assert
        assertThat(result).isTrue();
        verify(propagator).propagate(transportation, carrier);
    }

    @Test
    void testPropagateSuccessWithMultiplePropagators() {
        // Arrange
        SpanPropagator propagator1 = mock(SpanPropagator.class);
        SpanPropagator propagator2 = mock(SpanPropagator.class);
        SpanPropagatorRegistry registry = new SpanPropagatorRegistry(List.of(propagator1, propagator2));
        SpanTransportation transportation1 = new SpanTransportation("test1", "value1");
        SpanTransportation transportation2 = new SpanTransportation("test2", "value2");
        Object carrier = new Object();

        // propagator1 handles transportation1, propagator2 handles transportation2
        when(propagator1.propagate(transportation1, carrier)).thenReturn(true);
        when(propagator1.propagate(transportation2, carrier)).thenReturn(false);
        when(propagator2.propagate(transportation1, carrier)).thenReturn(false);
        when(propagator2.propagate(transportation2, carrier)).thenReturn(true);

        // Act
        boolean result = registry.propagate(List.of(transportation1, transportation2), carrier);

        // Assert
        assertThat(result).isTrue();
        verify(propagator1).propagate(transportation1, carrier);
        verify(propagator2).propagate(transportation2, carrier);
    }

    @Test
    void testPropagateFailureWhenNoPropagatorMatches() {
        // Arrange
        SpanPropagator propagator = mock(SpanPropagator.class);
        SpanPropagatorRegistry registry = new SpanPropagatorRegistry(List.of(propagator));
        SpanTransportation transportation = new SpanTransportation("test", "value");
        Object carrier = new Object();

        when(propagator.propagate(transportation, carrier)).thenReturn(false);

        // Act
        boolean result = registry.propagate(List.of(transportation), carrier);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void testCachingOfPropagator() {
        // Arrange
        SpanPropagator propagator = mock(SpanPropagator.class);
        SpanPropagatorRegistry registry = new SpanPropagatorRegistry(List.of(propagator));
        SpanTransportation transportation = new SpanTransportation("test", "value");
        Object carrier = new Object();

        when(propagator.propagate(transportation, carrier)).thenReturn(true);

        // Act
        registry.propagate(List.of(transportation), carrier); // Should populate cache
        verify(propagator, times(1)).propagate(transportation, carrier);

        boolean result = registry.propagate(List.of(transportation), carrier); // Should use cache

        // Assert
        assertThat(result).isTrue();
        // It should have called it twice in total (once during search, once during cached execution)
        // Wait, the search itself calls it!
        // First call:
        // knownPropagator = null
        // search: calls propagate(transportation, carrier) -> returns propagator -> calls it once.
        // caches it.
        // Second call:
        // knownPropagator = propagator
        // loop over transportations: success &= knownPropagator.propagate(transportation, carrier) -> calls it again.
        verify(propagator, times(2)).propagate(transportation, carrier);
    }

    @Test
    void testNoCachingWhenMultiplePropagatorsAreUsed() {
        // Arrange
        SpanPropagator propagator1 = mock(SpanPropagator.class);
        SpanPropagator propagator2 = mock(SpanPropagator.class);
        SpanPropagatorRegistry registry = new SpanPropagatorRegistry(List.of(propagator1, propagator2));
        SpanTransportation transportation1 = new SpanTransportation("test1", "value1");
        SpanTransportation transportation2 = new SpanTransportation("test2", "value2");
        Object carrier = new Object();

        when(propagator1.propagate(transportation1, carrier)).thenReturn(true);
        when(propagator2.propagate(transportation2, carrier)).thenReturn(true);

        // Act
        registry.propagate(List.of(transportation1, transportation2), carrier); // Should NOT populate cache for both

        // Second call should still search because it wasn't cached
        registry.propagate(List.of(transportation1, transportation2), carrier);

        // Assert
        // Each call should search both transportations.
        // Each transportation search calls the successful propagator once.
        verify(propagator1, times(2)).propagate(transportation1, carrier);
        verify(propagator2, times(2)).propagate(transportation2, carrier);
    }
}
