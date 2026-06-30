package com.wiredi.runtime.scheduler;

import jakarta.annotation.Nullable;

import java.time.Instant;

/**
 * Provides contextual timestamps about previous executions to compute the next trigger time.
 */
public interface TriggerContext {

    /**
     * The last scheduled execution time, if any.
     */
    @Nullable
    Instant lastScheduledExecution();

    /**
     * The last actual execution start time, if any.
     */
    @Nullable
    Instant lastActualExecution();

    /**
     * The last completion time (end of execution), if any.
     */
    @Nullable
    Instant lastCompletion();

    default boolean isFirstExecution() {
        return lastScheduledExecution() == null;
    }
}
