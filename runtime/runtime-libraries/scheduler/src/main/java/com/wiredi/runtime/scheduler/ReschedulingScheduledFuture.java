package com.wiredi.runtime.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A ScheduledFuture that represents a potentially infinite rescheduling sequence.
 * The future completes when canceled or when the trigger indicates no further executions.
 */
final class ReschedulingScheduledFuture implements ScheduledFuture<Object> {

    private volatile ScheduledFuture<?> current;
    private volatile boolean cancelled = false;
    private volatile boolean done = false;
    private final CountDownLatch completionLatch = new CountDownLatch(1);

    void updateDelegate(ScheduledFuture<?> next) {
        if (cancelled || done) {
            if (next != null) {
                next.cancel(true);
            }
            return;
        }
        this.current = Objects.requireNonNull(next, "delegate future");
    }

    void complete() {
        done = true;
        completionLatch.countDown();
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        ScheduledFuture<?> c = current;
        if (c != null) {
            return c.getDelay(unit);
        }
        return 0L;
    }

    @Override
    public int compareTo(Delayed o) {
        ScheduledFuture<?> c = current;
        if (c != null) {
            return c.compareTo(o);
        }
        long d1 = getDelay(TimeUnit.NANOSECONDS);
        long d2 = o.getDelay(TimeUnit.NANOSECONDS);
        return Long.compare(d1, d2);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        cancelled = true;
        ScheduledFuture<?> c = current;
        if (c != null) {
            c.cancel(mayInterruptIfRunning);
        }
        complete();
        return true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        return done || cancelled;
    }

    @Override
    public Object get() throws InterruptedException {
        completionLatch.await();
        return null;
    }

    @Override
    public Object get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, TimeoutException {
        if (completionLatch.await(timeout, unit)) {
            return null;
        }
        throw new TimeoutException("Timeout while waiting for rescheduling future to complete");
    }
}
