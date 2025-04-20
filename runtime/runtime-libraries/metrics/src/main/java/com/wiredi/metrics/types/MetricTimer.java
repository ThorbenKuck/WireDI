package com.wiredi.metrics.types;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Interface for timing metrics that can record durations and execution times of operations.
 *
 * Provides various methods to record timing data and retrieve statistics.
 */
public interface MetricTimer {
    /**
     * Records a specific duration in the timer.
     *
     * @param duration the duration to record
     */
    void record(Duration duration);

    /**
     * Records a duration specified by an amount and time unit.
     *
     * @param amount the amount of time
     * @param unit   the time unit of the amount parameter
     */
    void record(long amount, TimeUnit unit);

    /**
     * Records the execution time of a runnable operation.
     *
     * @param runnable the operation whose execution time should be recorded
     */
    void record(Runnable runnable);

    /**
     * Records the execution time of a supplier operation and returns its result.
     *
     * @param supplier the operation whose execution time should be recorded
     * @param <T>      the type of the result
     * @return the result of the supplier operation
     */
    <T> T record(Supplier<T> supplier);

    /**
     * Returns the number of all recorded timings.
     *
     * @return the count of recordings
     */
    long count();

    /**
     * Returns the total time of all recordings.
     *
     * @return the total recorded time
     */
    double totalTime();

    /**
     * Closes this timer and releases any system resources associated with it.
     */
    void close();
}
