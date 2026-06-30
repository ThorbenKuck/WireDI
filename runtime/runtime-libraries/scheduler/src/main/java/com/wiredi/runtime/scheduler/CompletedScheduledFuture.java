package com.wiredi.runtime.scheduler;

import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A ScheduledFuture that is already completed successfully.
 */
final class CompletedScheduledFuture implements ScheduledFuture<Object> {

    static final CompletedScheduledFuture INSTANCE = new CompletedScheduledFuture();

    private CompletedScheduledFuture() {
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return 0;
    }

    @Override
    public int compareTo(Delayed o) {
        long d2 = o.getDelay(TimeUnit.NANOSECONDS);
        return Long.compare(0L, d2);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public Object get() {
        return null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) {
        return null;
    }
}
