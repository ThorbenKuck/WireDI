package com.wiredi.metrics.types;


/**
 * Represents a metric counter that can be incremented or decremented.
 * This interface provides methods to track and manipulate a numeric counter value
 * that can be used for various monitoring and metrics purposes.
 */
public interface MetricCounter {
    /**
     * Increments the counter value by 1.
     */
    void increment();

    /**
     * Increments the counter value by the specified amount.
     *
     * @param amount the value to increment the counter by
     */
    void increment(long amount);

    /**
     * Decrements the counter value by 1.
     */
    void decrement();

    /**
     * Decrements the counter value by the specified amount.
     *
     * @param amount the value to decrement the counter by
     */
    void decrement(long amount);

    /**
     * Returns the current value of the counter.
     *
     * @return the current counter value
     */
    long count();

    /**
     * Closes this counter and releases any resources associated with it.
     */
    void close();
}
