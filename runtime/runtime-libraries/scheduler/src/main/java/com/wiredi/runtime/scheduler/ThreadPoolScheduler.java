package com.wiredi.runtime.scheduler;

import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple Scheduler implementation backed by ScheduledThreadPoolExecutor.
 * Supports scheduling at an Instant, fixed rate, fixed delay and Trigger-based (e.g. Cron) execution.
 */
public class ThreadPoolScheduler implements Scheduler, AutoCloseable {

    private final ScheduledThreadPoolExecutor executor;
    private final Clock clock;

    /**
     * Create a scheduler with a single worker thread.
     */
    public ThreadPoolScheduler() {
        this(1);
    }

    /**
     * Create a scheduler with the given pool size.
     *
     * @param poolSize number of scheduler threads
     */
    public ThreadPoolScheduler(int poolSize) {
        this(poolSize, defaultThreadFactory("simple-scheduler-"), Clock.systemDefaultZone());
    }

    /**
     * Create a scheduler with custom ThreadFactory and Clock.
     *
     * @param poolSize      number of scheduler threads
     * @param threadFactory thread factory to create worker threads
     * @param clock         clock for time calculations
     */
    public ThreadPoolScheduler(int poolSize, @NotNull ThreadFactory threadFactory, @NotNull Clock clock) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException("poolSize must be > 0");
        }
        this.executor = new ScheduledThreadPoolExecutor(poolSize, Objects.requireNonNull(threadFactory, "threadFactory"));
        this.executor.setRemoveOnCancelPolicy(true);
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    private static ThreadFactory defaultThreadFactory(@NotNull String prefix) {
        AtomicInteger idx = new AtomicInteger(1);
        return r -> {
            Thread t = new Thread(r, prefix + idx.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
    }

    @Override
    public @NotNull Clock getClock() {
        return this.clock;
    }

    @Override
    @NotNull
    public <E extends Throwable> ScheduledFuture<?> schedule(
            @NotNull Task<E> task,
            @NotNull Trigger trigger
    ) {
        SchedulerExecutionEnvironment environment = new SchedulerExecutionEnvironment(clock, trigger);

        Duration initialDelay = environment.prepareNextRun();
        if (initialDelay == null) {
            // Nothing to schedule
            return CompletedScheduledFuture.INSTANCE;
        }

        ScheduledRunnableWrapper runnable = new ScheduledRunnableWrapper(environment, task, executor);

        ScheduledFuture<?> firstFuture = executor.schedule(runnable, initialDelay.toMillis(), TimeUnit.MILLISECONDS);
        environment.updateFuture(firstFuture);

        return environment.getFuture();
    }

    /**
     * Gracefully shuts down the scheduler. Pending tasks will not be started, running tasks continue.
     */
    @Override
    public void close() {
        executor.shutdownNow();
    }
}
