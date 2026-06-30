package com.wiredi.runtime.scheduler;

import jakarta.annotation.Nullable;

import java.time.Instant;

/**
 * Default TriggerContext implementation holding timestamps of the last execution cycle.
 */
final class SimpleTriggerContext implements TriggerContext {

    @Nullable
    private volatile Instant lastScheduled;
    @Nullable
    private volatile Instant lastActual;
    @Nullable
    private volatile Instant lastCompletion;

    @Override
    @Nullable
    public Instant lastScheduledExecution() {
        return lastScheduled;
    }

    @Override
    @Nullable
    public Instant lastActualExecution() {
        return lastActual;
    }

    @Override
    @Nullable
    public Instant lastCompletion() {
        return lastCompletion;
    }

    void setLastScheduled(@Nullable Instant lastScheduled) {
        this.lastScheduled = lastScheduled;
    }

    void setLastStart(@Nullable Instant lastActual) {
        this.lastActual = lastActual;
    }

    void setLastCompletion(@Nullable Instant lastCompletion) {
        this.lastCompletion = lastCompletion;
    }
}
