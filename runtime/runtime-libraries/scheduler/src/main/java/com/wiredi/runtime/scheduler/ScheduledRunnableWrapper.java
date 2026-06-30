package com.wiredi.runtime.scheduler;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class ScheduledRunnableWrapper implements Runnable {

    private final SchedulerExecutionEnvironment environment;
    private final Task<?> task;
    private final ScheduledExecutorService executor;

    public ScheduledRunnableWrapper(
            SchedulerExecutionEnvironment environment,
            Task<?> task,
            ScheduledExecutorService executor
    ) {
        this.environment = environment;
        this.task = task;
        this.executor = executor;
    }

    @Override
    public void run() {
        if (environment.triggerContext().isFirstExecution()) {
            task.started();
        }

        if (environment.tryComplete()) {
            task.completed();
            return;
        }

        environment.runTask(task);

        if (environment.tryComplete()) {
            task.completed();
            return;
        }

        Duration nextDelay = environment.prepareNextRun();
        if (nextDelay == null) {
            environment.complete();
            task.completed();
            return;
        }

        ScheduledFuture<?> nextFuture = executor.schedule(this, nextDelay.toMillis(), TimeUnit.MILLISECONDS);
        environment.updateFuture(nextFuture);
    }
}
