package com.wiredi.telemetry.metrics;

import com.wiredi.runtime.lang.AppClock;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InMemoryTimerTypeTest {

    @Test
    void testRecordValue() {
        // Arrange
        InMemoryTimerType timer = new InMemoryTimerType();

        // Act
        timer.record(100, TimeUnit.MILLISECONDS);

        // Assert
        assertThat(timer.totalTime()).isEqualTo(Duration.ofMillis(100));
        assertThat(timer.count()).isEqualTo(Duration.ofMillis(100).toNanos());
    }

    @Test
    void testRecordDuration() {
        // Arrange
        InMemoryTimerType timer = new InMemoryTimerType();

        // Act
        timer.record(Duration.ofSeconds(2));

        // Assert
        assertThat(timer.totalTime()).isEqualTo(Duration.ofSeconds(2));
    }

    @Test
    void testRecordRunnableWithMockClock() {
        // Arrange
        AppClock clock = mock(AppClock.class);
        InMemoryTimerType timer = new InMemoryTimerType(clock);
        when(clock.nanoTime()).thenReturn(1000L, 3000L);

        // Act
        timer.record(() -> {});

        // Assert
        assertThat(timer.totalTime()).isEqualTo(Duration.ofNanos(2000));
        verify(clock, times(2)).nanoTime();
    }

    @Test
    void testRecordSupplierWithMockClock() {
        // Arrange
        AppClock clock = mock(AppClock.class);
        InMemoryTimerType timer = new InMemoryTimerType(clock);
        when(clock.nanoTime()).thenReturn(5000L, 8000L);

        // Act
        String result = timer.record((java.util.function.Supplier<String>) () -> "test");

        // Assert
        assertThat(result).isEqualTo("test");
        assertThat(timer.totalTime()).isEqualTo(Duration.ofNanos(3000));
    }

    @Test
    void testRecordCallableWithMockClock() throws Exception {
        // Arrange
        AppClock clock = mock(AppClock.class);
        InMemoryTimerType timer = new InMemoryTimerType(clock);
        when(clock.nanoTime()).thenReturn(10000L, 15000L);

        // Act
        String result = timer.record((java.util.concurrent.Callable<String>) () -> "callable");

        // Assert
        assertThat(result).isEqualTo("callable");
        assertThat(timer.totalTime()).isEqualTo(Duration.ofNanos(5000));
    }
}
