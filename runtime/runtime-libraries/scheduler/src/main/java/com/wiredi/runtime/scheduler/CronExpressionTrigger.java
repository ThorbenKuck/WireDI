package com.wiredi.runtime.scheduler;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import jakarta.annotation.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * A Trigger implementation that computes next execution times from a Cron expression.
 * Uses Quartz-like cron definitions by default (supports seconds).
 */
public final class CronExpressionTrigger implements Trigger {

    private final Cron cron;
    private final ZoneId zoneId;
    private final ExecutionTime executionTime;

    public CronExpressionTrigger(Cron cron, ZoneId zoneId) {
        this.executionTime = ExecutionTime.forCron(cron);
        this.cron = cron;
        this.zoneId = zoneId;
    }

    public Cron getCron() {
        return cron;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public ExecutionTime getExecutionTime() {
        return executionTime;
    }

    @Override
    @Nullable
    public Instant nextExecution(TriggerContext context) {
        ZonedDateTime reference;

        if (context != null) {
            Instant base = context.lastCompletion();
            if (base == null) {
                base = context.lastActualExecution();
            }
            if (base == null) {
                base = context.lastScheduledExecution();
            }
            if (base != null) {
                reference = ZonedDateTime.ofInstant(base, zoneId);
            } else {
                reference = ZonedDateTime.now(zoneId);
            }
        } else {
            reference = ZonedDateTime.now(zoneId);
        }

        Optional<ZonedDateTime> next = executionTime.nextExecution(reference);
        return next.map(ZonedDateTime::toInstant).orElse(null);
    }
}
