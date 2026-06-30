package com.wiredi.runtime.scheduler.trigger;

import com.wiredi.runtime.scheduler.StartStrategy;
import com.wiredi.runtime.scheduler.Trigger;
import com.wiredi.runtime.scheduler.TriggerContext;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

public record FixedRateTrigger(
        Duration period,
        StartStrategy startStrategy
) implements Trigger {

    @Override
    public @Nullable Instant nextExecution(TriggerContext context) {
        Instant last = context.lastScheduledExecution();
        if (last == null) {
            return startStrategy.resolveBase();
        }

        return last.plus(period);
    }
}
