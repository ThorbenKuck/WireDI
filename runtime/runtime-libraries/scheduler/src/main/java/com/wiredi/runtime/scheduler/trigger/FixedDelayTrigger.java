package com.wiredi.runtime.scheduler.trigger;

import com.wiredi.runtime.scheduler.StartStrategy;
import com.wiredi.runtime.scheduler.Trigger;
import com.wiredi.runtime.scheduler.TriggerContext;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

public record FixedDelayTrigger(
        Duration delay,
        StartStrategy startStrategy
) implements Trigger {

    @Override
    public @Nullable Instant nextExecution(TriggerContext context) {
        Instant base = context.lastCompletion();
        if (base == null) {
            base = context.lastActualExecution();
        }
        if (base == null) {
            base = context.lastScheduledExecution();
        }
        if (base == null) {
            return startStrategy.resolveBase();
        }
        return base.plus(delay);

    }
}
