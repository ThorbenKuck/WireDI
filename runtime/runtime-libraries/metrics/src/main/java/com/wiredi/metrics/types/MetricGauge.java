package com.wiredi.metrics.types;

/**
 * Represents a metric gauge that can provide measurements of a specific value over time.
 * A gauge is a metric that represents a single numerical value that can arbitrarily go up and down.
 */
public interface MetricGauge {

    /**
     * Measures and returns the current set of measurements for this gauge.
     *
     * @return An iterable of measurements representing the current state of the gauge
     */
    Iterable<? extends Measurement> measure();

    /**
     * Represents a single measurement value taken from a metric gauge.
     * Each measurement contains a numeric value that represents the state at a specific point in time.
     */
    interface Measurement {
        /**
         * Gets the numeric value of this measurement.
         *
         * @return The double value representing this measurement
         */
        double getValue();
    }
}
