package com.wiredi.telemetry.metrics;

import com.wiredi.telemetry.TelemetryTag;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryMetricsTest {

    private final InMemoryMetrics metrics = new InMemoryMetrics();

    @Test
    void testCounterCreationAndBehavior() {
        // Arrange
        String name = "test.counter";
        Iterable<TelemetryTag> tags = List.of(new TelemetryTag("key", "value"));

        // Act
        CounterType counter = metrics.counter(name, tags);
        CounterType counter2 = metrics.counter(name, tags);
        counter.increment(5.0);
        counter.increment(2);

        // Assert
        assertThat(counter).isNotNull();
        assertThat(counter).isSameAs(counter2);
        assertThat(counter.count()).isEqualTo(7.0);
    }

    @Test
    void testGaugeCreationAndBehavior() {
        // Arrange
        String name = "test.gauge";
        Iterable<TelemetryTag> tags = List.of(new TelemetryTag("key", "value"));
        List<String> state = List.of("a", "b");

        // Act
        GaugeType<List<String>> gauge = metrics.gauge(name, tags, state, List::size);
        GaugeType<List<String>> gauge2 = metrics.gauge(name, tags, state, List::size);

        // Assert
        assertThat(gauge).isNotNull();
        assertThat(gauge).isSameAs(gauge2);
        assertThat(gauge.state()).isSameAs(state);
        assertThat(gauge.value()).isEqualTo(2.0);
    }

    @Test
    void testGaugeWithDifferentStateThrowsException() {
        // Arrange
        String name = "test.gauge";
        Iterable<TelemetryTag> tags = List.of(new TelemetryTag("key", "value"));
        
        metrics.gauge(name, tags, new Object(), obj -> 1.0);

        // Act
        org.assertj.core.api.ThrowableAssert.ThrowingCallable callable = () -> metrics.gauge(name, tags, new Object(), obj -> 1.0);

        // Assert
        assertThatThrownBy(callable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Gauge already exists with different state");
    }

    @Test
    void testTimerCreationAndBehavior() {
        // Arrange
        String name = "test.timer";
        Iterable<TelemetryTag> tags = List.of(new TelemetryTag("key", "value"));

        // Act
        TimerType timer = metrics.timer(name, tags);
        TimerType timer2 = metrics.timer(name, tags);
        
        timer.record(100, TimeUnit.MILLISECONDS);
        timer.record(Duration.ofSeconds(1));

        // Assert
        assertThat(timer).isNotNull();
        assertThat(timer).isSameAs(timer2);
        // Total time should be 1.1 seconds
        assertThat(timer.totalTime()).isEqualTo(Duration.ofMillis(1100));
        // In current implementation, count() returns total nanos
        assertThat(timer.count()).isEqualTo(Duration.ofMillis(1100).toNanos());
    }

    @Test
    void testTimerRecordRunnable() {
        // Arrange
        TimerType timer = metrics.timer("test.runnable", List.of());

        // Act
        timer.record((Runnable) () -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Assert
        assertThat(timer.totalTime()).isGreaterThanOrEqualTo(Duration.ofMillis(10));
    }

    @Test
    void testTimerRecordSupplier() {
        // Arrange
        TimerType timer = metrics.timer("test.supplier", List.of());

        // Act
        String result = timer.record((java.util.function.Supplier<String>) () -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "done";
        });

        // Assert
        assertThat(result).isEqualTo("done");
        assertThat(timer.totalTime()).isGreaterThanOrEqualTo(Duration.ofMillis(10));
    }
}
