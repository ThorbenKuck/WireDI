package com.wiredi.runtime.scheduler;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.wiredi.runtime.scheduler.trigger.JustOnceTrigger;
import jakarta.annotation.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * Represents a strategy to calculate the next execution time for a scheduled task.
 */
public interface Trigger {

    static Trigger cron(String expression) {
        return cron(expression, ZoneId.systemDefault(), CronType.QUARTZ);
    }

    static Trigger cron(String expression, ZoneId zoneId) {
        return cron(expression, zoneId, CronType.QUARTZ);
    }

    static Trigger cron(String expression, CronType cronType) {
        return cron(expression, ZoneId.systemDefault(), cronType);
    }

    static Trigger cron(String expression, ZoneId zoneId, CronType cronType) {
        CronDefinition definition = CronDefinitionBuilder.instanceDefinitionFor(cronType);
        CronParser parser = new CronParser(definition);
        return new CronExpressionTrigger(parser.parse(expression), zoneId);
    }

    static Trigger once() {
        return new JustOnceTrigger(StartStrategy.IMMEDIATE);
    }

    // One-shot: in X time
    static Trigger onceIn(long amount, TimeUnit unit) {
        return onceIn(Duration.ofMillis(unit.toMillis(amount)));
    }

    static Trigger onceIn(Duration delay) {
        return new Trigger() {
            @Override
            public Instant nextExecution(TriggerContext context) {
                if (context.lastScheduledExecution() == null) {
                    return Instant.now().plus(delay);
                }
                return null;
            }
        };
    }

    // One-shot: at time
    static Trigger onceAt(Instant instant) {
        return new Trigger() {
            @Override
            public Instant nextExecution(TriggerContext context) {
                if (context.lastScheduledExecution() == null) {
                    return instant;
                }
                return null;
            }
        };
    }

    // Fixed rate: first now
    static Trigger every(Duration period) {
        return every(period, null);
    }

    // Fixed rate: first at startTime
    static Trigger every(Duration period, Instant startTime) {
        if (period.isZero() || period.isNegative()) {
            throw new IllegalArgumentException("period must be > 0");
        }
        return context -> {
            Instant last = context.lastScheduledExecution();
            if (last == null) {
                return startTime != null ? startTime : Instant.now();
            }
            return last.plus(period);
        };
    }

    // Fixed delay: first now
    static Trigger withFixedDelay(Duration delay) {
        return withFixedDelay(null, delay);
    }

    // Fixed delay: first at startTime
    static Trigger withFixedDelay(Instant startTime, Duration delay) {
        if (delay.isZero() || delay.isNegative()) {
            throw new IllegalArgumentException("delay must be > 0");
        }
        return context -> {
            Instant lastCompletion = context.lastCompletion();
            if (lastCompletion != null) {
                return lastCompletion.plus(delay);
            }
            Instant lastActual = context.lastActualExecution();
            if (lastActual != null) {
                return lastActual.plus(delay);
            }
            Instant lastScheduled = context.lastScheduledExecution();
            if (lastScheduled != null) {
                return lastScheduled.plus(delay);
            }
            return startTime != null ? startTime : Instant.now();
        };
    }

    static TriggerBuilder builder() {
        return TriggerBuilder.create();
    }

    /**
     * Calculate the next execution time, or return null if no further execution should be scheduled.
     *
     * @param context contextual information about previous executions (may be null values inside)
     * @return the next execution Instant or null
     */
    @Nullable
    Instant nextExecution(TriggerContext context);

}
