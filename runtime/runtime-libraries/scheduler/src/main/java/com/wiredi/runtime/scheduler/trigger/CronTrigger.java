package com.wiredi.runtime.scheduler.trigger;

import com.cronutils.model.time.ExecutionTime;
import com.wiredi.runtime.scheduler.StartStrategy;
import com.wiredi.runtime.scheduler.Trigger;
import com.wiredi.runtime.scheduler.TriggerContext;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public record CronTrigger(
        StartStrategy startStrategy,
        ZoneId zoneId,
        ExecutionTime executionTime
) implements Trigger {

    @Override
    public @Nullable Instant nextExecution(TriggerContext context) {
        // First scheduling
        if (context.lastScheduledExecution() == null &&
                context.lastActualExecution() == null &&
                context.lastCompletion() == null) {
            ZonedDateTime reference = ZonedDateTime.ofInstant(startStrategy.resolveBase(), zoneId);
            Optional<ZonedDateTime> next = executionTime.nextExecution(reference);
            return next.map(ZonedDateTime::toInstant).orElse(null);
        }

        // Subsequent scheduling uses completion -> actual -> scheduled
        Instant base = context.lastCompletion();
        if (base == null) {
            base = context.lastActualExecution();
        }
        if (base == null) {
            base = context.lastScheduledExecution();
        }
        if (base == null) {
            base = startStrategy.resolveBase();
        }
        ZonedDateTime reference = ZonedDateTime.ofInstant(base, zoneId);
        Optional<ZonedDateTime> next = executionTime.nextExecution(reference);
        return next.map(ZonedDateTime::toInstant).orElse(null);

    }
}
