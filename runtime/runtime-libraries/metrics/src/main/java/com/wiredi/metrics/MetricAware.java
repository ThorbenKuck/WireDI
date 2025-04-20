package com.wiredi.metrics;

/**
 * Interface for components that need to be aware of and utilize metrics collection capabilities.
 * <p>
 * Components implementing this interface will automatically receive a {@link MetricCollector} instance
 * when they are registered in the WireD container with the metrics integration enabled. This allows
 * for consistent metrics collection across different parts of an application without tight coupling
 * to a specific metrics implementation.
 * <p>
 * When a {@link MetricCollector} is created in the application context, all instances implementing
 * this interface will automatically have their {@link #setMetricCollector(MetricCollector)} method
 * called with the appropriate collector instance.
 * <p>
 * Example usage:
 * <pre>{@code
 * public class RequestProcessor implements MetricAware {
 *     private MetricCollector metrics;
 *     private MetricCounter requestCounter;
 *     private MetricTimer processingTimer;
 *
 *     @Override
 *     public void setMetricCollector(MetricCollector collector) {
 *         this.metrics = collector;
 *         this.requestCounter = metrics.counter("requests.processed");
 *         this.processingTimer = metrics.timer("request.processing.time");
 *     }
 *
 *     public void processRequest(Request request) {
 *         requestCounter.increment();
 *         timer.record(() -> {
 *             // Process the request
 *         });
 *     }
 * }
 * }</pre>
 *
 * @see MetricCollector
 */
public interface MetricAware {
    /**
     * Sets the metric collector for this component.
     * <p>
     * This method is called automatically by the metrics integration when a {@link MetricCollector}
     * is available in the application context.
     * Implementations should use this collector to create and register their metrics.
     * <p>
     * Implementations should not assume that this method will be called exactly once or that
     * the same collector instance will always be provided. They should properly handle multiple
     * calls with potentially different collector instances.
     *
     * @param collector the metric collector to use for creating and recording metrics
     */
    void setMetricCollector(MetricCollector collector);
}
