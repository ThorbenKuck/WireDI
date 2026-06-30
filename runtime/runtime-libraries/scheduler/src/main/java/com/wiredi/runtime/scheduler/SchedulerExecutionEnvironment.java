package com.wiredi.runtime.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

class SchedulerExecutionEnvironment {

    private final ReschedulingScheduledFuture future;
    private final Clock clock;
    private final SimpleTriggerContext triggerContext;
    private final Trigger trigger;

    private static final Logger logger = LoggerFactory.getLogger(SchedulerExecutionEnvironment.class);

    SchedulerExecutionEnvironment(Clock clock, Trigger trigger) {
        this.clock = clock;
        this.trigger = trigger;
        this.future = new ReschedulingScheduledFuture();
        this.triggerContext = new SimpleTriggerContext();
    }

    public TriggerContext triggerContext() {
        return triggerContext;
    }

    public boolean tryComplete() {
        if (future.isCancelled()) {
            complete();
            return true;
        }

        return false;
    }

    public ReschedulingScheduledFuture getFuture() {
        return future;
    }

    public void complete() {
        future.complete();
    }

    public void taskStarted() {
        Instant instant = clock.instant();
        triggerContext.setLastStart(instant);
    }

    public void taskCompleted() {
        Instant instant = clock.instant();
        triggerContext.setLastCompletion(instant);
    }

    public void updateFuture(ScheduledFuture<?> future) {
        this.future.updateDelegate(future);
    }

    public Duration prepareNextRun() {
        Instant next = trigger.nextExecution(triggerContext);
        if (next == null) {
            return null;
        }

        triggerContext.setLastScheduled(next);
        Duration remainingDuration = Duration.between(clock.instant(), next);
        if (remainingDuration.isPositive()) {
            return remainingDuration;
        } else {
            return Duration.ZERO;
        }
    }

    public void runTask(Task<?> task) {
        taskStarted();
        try {
            task.run();
        } catch (Throwable t) {
            try {
                task.handleError(t);
            } catch (Throwable nested) {
                logger.warn("Task threw error while handling error. Original error: \"{}\". Nested error: \"{}\"", t.getMessage(), nested.getMessage());
            }
        } finally {
            taskCompleted();
        }
    }
}
