package com.wiredi.metrics;

import com.wiredi.metrics.types.MetricCounter;
import com.wiredi.metrics.types.MetricGauge;
import com.wiredi.metrics.types.MetricTimer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MicrometerMetricCollectorTest {

    private MeterRegistry meterRegistry;
    private MicrometerMetricCollector collector;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        collector = new MicrometerMetricCollector(meterRegistry);
    }

    @Test
    void shouldIncrementCounter() {
        // given
        String name = "test.counter";

        // when
        MetricCounter counter = collector.counter(name, MetricTag.of("tag1", "value1"));
        counter.increment();

        // then
        assertEquals(1.0, meterRegistry.get(name).counter().count());
    }

    @Test
    void shouldRecordGaugeValue() {
        // given
        String name = "test.gauge";

        // when
        MetricGauge gauge = collector.gauge(name, () -> 42.0, MetricTag.of("tag1", "value1"));

        // then
        assertEquals(42.0, meterRegistry.get(name).gauge().value());
        Iterator<? extends MetricGauge.Measurement> measurements = gauge.measure().iterator();
        assertTrue(measurements.hasNext());
        assertEquals(42.0, measurements.next().getValue());
        assertFalse(measurements.hasNext());
    }

    @Test
    void shouldRecordTimings() {
        // given
        String name = "test.timer";

        // when
        MetricTimer timer = collector.timer(name, MetricTag.of("tag1", "value1"));
        timer.record(Duration.ofMillis(100));

        // then
        assertTrue(meterRegistry.get(name).timer().count() > 0);
        assertEquals(100, meterRegistry.get(name).timer().totalTime(java.util.concurrent.TimeUnit.MILLISECONDS));
    }
}