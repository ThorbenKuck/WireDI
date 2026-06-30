package com.wiredi.runtime.scheduler;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.wiredi.runtime.scheduler.trigger.CronTrigger;
import com.wiredi.runtime.scheduler.trigger.FixedDelayTrigger;
import com.wiredi.runtime.scheduler.trigger.FixedRateTrigger;
import com.wiredi.runtime.scheduler.trigger.JustOnceTrigger;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Fluent builder for composing Triggers with start time/after + strategy (cron/fixedRate/fixedDelay/once).
 */
public final class TriggerBuilder {

    private Instant startAt;
    private Duration after;

    private StartStrategy startStrategy;

    private TriggerBuilder() {
    }

    public static TriggerBuilder create() {
        return new TriggerBuilder();
    }

    public Trigger atFixedRate(Duration period) {
        if (period.isZero() || period.isNegative()) {
            throw new IllegalArgumentException("period must be > 0");
        }

        return new FixedRateTrigger(period, startStrategy);
    }

    public Trigger withFixedDelay(Duration delay) {
        if (delay.isZero() || delay.isNegative()) {
            throw new IllegalArgumentException("delay must be > 0");
        }

        return new FixedDelayTrigger(delay, startStrategy);
    }

    public Trigger cron(Consumer<CronBuilder> consumer) {
        final TriggerBuilder.CronBuilder builder = cron();
        consumer.accept(builder);
        return builder.build();
    }

    public TriggerBuilder.CronBuilder cron() {
        return new TriggerBuilder.CronBuilder(startStrategy);
    }

    public Trigger once() {
        return new JustOnceTrigger(startStrategy);
    }

    public TriggerBuilder startupStrategy(StartStrategy strategy) {
        this.startStrategy = strategy;
        return this;
    }

    public TriggerBuilder startAt(Instant instant) {
        return startupStrategy(() -> instant);
    }

    public TriggerBuilder startAfter(Duration duration) {
        return startupStrategy(() -> Instant.now().plus(duration));
    }

    public static class CronBuilder {
        private String cronExpression;
        private ZoneId zoneId = ZoneId.systemDefault();
        private CronType cronType = CronType.QUARTZ;
        private final StartStrategy startStrategy;

        private CronBuilder(StartStrategy startStrategy) {
            this.startStrategy = startStrategy;
        }

        public CronBuilder expression(String expression) {
            this.cronExpression = expression;
            return this;
        }

        public CronBuilder zoneId(ZoneId zoneId) {
            this.zoneId = zoneId;
            return this;
        }

        public CronBuilder cronType(CronType cronType) {
            this.cronType = cronType;
            return this;
        }

        public Trigger build() {
            if (cronExpression == null) {
                throw new IllegalStateException("expression must be set");
            }

            // Build a cron-based trigger honoring the start strategy for the first execution
            final CronDefinition definition = CronDefinitionBuilder.instanceDefinitionFor(cronType);
            final CronParser parser = new CronParser(definition);
            final Cron cron = parser.parse(cronExpression);
            final ExecutionTime executionTime = ExecutionTime.forCron(cron);

            return new CronTrigger(startStrategy, zoneId, executionTime);
        }
    }
}
