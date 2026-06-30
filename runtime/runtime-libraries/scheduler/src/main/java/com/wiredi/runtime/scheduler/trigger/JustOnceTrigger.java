package com.wiredi.runtime.scheduler.trigger;

import com.wiredi.runtime.scheduler.StartStrategy;
import com.wiredi.runtime.scheduler.Trigger;
import com.wiredi.runtime.scheduler.TriggerContext;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public record JustOnceTrigger(
        StartStrategy startStrategy
) implements Trigger {

    @Override
    public @Nullable Instant nextExecution(TriggerContext context) {
        if (context.lastScheduledExecution() == null) {
            return startStrategy.resolveBase();
        }

        return null;
    }
}
