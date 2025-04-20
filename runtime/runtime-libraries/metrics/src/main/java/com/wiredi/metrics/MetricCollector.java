package com.wiredi.metrics;

import com.wiredi.metrics.types.MetricGauge;
import com.wiredi.metrics.types.MetricCounter;
import com.wiredi.metrics.types.MetricTimer;

import java.util.function.Supplier;

/**
 * Centralized interface for collecting and recording application metrics across different dimensions.
 *
 * <p>MetricCollector provides a standardized abstraction layer for application instrumentation,
 * allowing metrics to be captured and reported independently of the underlying metrics implementation.
 * This abstraction enables switching between different metrics providers (like Micrometer, Prometheus,
 * or custom implementations) without changing application code.</p>
 *
 * <p>The interface supports three primary metric types:</p>
 * <ul>
 *   <li><b>Counters</b> - For tracking counts of events or occurrences (e.g., request counts, error counts)</li>
 *   <li><b>Timers</b> - For measuring durations of operations (e.g., request processing time, method execution time)</li>
 *   <li><b>Gauges</b> - For observing current values that can fluctuate (e.g., queue depth, connection pool size)</li>
 * </ul>
 *
 * <p>All metrics can be tagged with dimensional metadata using {@link MetricTag} instances, allowing
 * metrics to be filtered, grouped, and analyzed across different dimensions in monitoring systems.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Inject the MetricCollector
 * @Inject
 * private MetricCollector metrics;
 *
 * // Create a counter and increment it
 * MetricCounter requestCounter = metrics.counter("http.requests",
 *     new MetricTag("endpoint", "/api/users"));
 * requestCounter.increment();
 *
 * // Time an operation
 * MetricTimer timer = metrics.timer("operation.duration",
 *     new MetricTag("operation", "database-query"));
 * try (var ctx = timer.time()) {
 *     // Perform the timed operation
 *     performDatabaseQuery();
 * }
 *
 * // Track a gauge value that changes over time
 * metrics.gauge("queue.size",
 *     () -> (double) workQueue.size(),
 *     new MetricTag("queue", "work-items"));
 * }</pre>
 *
 * <p>For testing scenarios where metrics collection is not needed, use the {@link #noop()} method
 * to obtain a no-operation implementation that discards all metrics.</p>
 *
 * @see MetricCounter
 * @see MetricTimer
 * @see MetricGauge
 * @see MetricTag
 */
public interface MetricCollector {

    /**
     * Returns a no-operation implementation of MetricCollector that discards all metrics.
     *
     * <p>This implementation is useful in testing environments or when metrics collection
     * needs to be disabled without changing application code.</p>
     *
     * @return a new instance of a no-operation MetricCollector
     */
    static MetricCollector noop() {
        return new NoopMetricCollector();
    }

    /**
     * Creates a counter metric with the specified name and optional tags.
     *
     * <p>Counters are monotonically increasing values used to count occurrences of events.
     * They can only be incremented, not decremented.</p>
     *
     * @param name the name of the counter metric
     * @param tags optional tags to associate with this counter for dimensional metrics
     * @return a new counter metric that can be incremented
     */
    MetricCounter counter(String name, MetricTag... tags);

    /**
     * Creates a timer metric with the specified name and optional tags.
     *
     * <p>Timers measure the duration of operations and record timing statistics
     * including count, total time, and various distribution percentiles.</p>
     *
     * @param name the name of the timer metric
     * @param tags optional tags to associate with this timer for dimensional metrics
     * @return a new timer metric that can measure operation durations
     */
    MetricTimer timer(String name, MetricTag... tags);

    /**
     * Creates a gauge metric with the specified name, value supplier function, and optional tags.
     *
     * <p>Gauges represent a current value that can fluctuate over time. The supplied function
     * will be called whenever the current value of the gauge is requested by the metrics system.</p>
     *
     * @param name the name of the gauge metric
     * @param function a supplier function that returns the current double value for the gauge
     * @param tags optional tags to associate with this gauge for dimensional metrics
     * @return a new gauge metric that reports values from the supplied function
     */
    MetricGauge gauge(String name, Supplier<Double> function, MetricTag... tags);

}
