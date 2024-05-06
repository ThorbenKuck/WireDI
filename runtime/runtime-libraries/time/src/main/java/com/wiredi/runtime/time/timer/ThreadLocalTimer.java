package com.wiredi.runtime.time.timer;

import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.time.timer.exceptions.TimerAlreadyStartedException;
import com.wiredi.runtime.time.timer.exceptions.TimerNotStartedException;
import com.wiredi.runtime.time.timer.interpreter.TimeContext;
import org.jetbrains.annotations.NotNull;

/**
 * A native thread local implementation of the Timer interface.
 * <p>
 * This class will utilize {@link ThreadLocal} to measure start nanos.
 * A ThreadLocalTimer is only considered started if the thread calling {@link #stop()} has previously called
 * {@link #start()}.
 */
public class ThreadLocalTimer implements Timer {

    private final TimeContext context;
    private final ThreadLocal<Long> startNanos = new ThreadLocal<>();

    public ThreadLocalTimer(TimeContext context) {
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStarted() {
        return startNanos.get() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        if (isStarted()) {
            throw new TimerAlreadyStartedException("Timer is already running");
        }

        startNanos.set(context.current());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Timed stop() {
        long stopNanos = context.current();
        if (!isStarted()) {
            throw new TimerNotStartedException("Timer was not started");
        }

        long startNanos = this.startNanos.get();
        this.startNanos.remove();
        return new Timed(context.toNanos(stopNanos - startNanos));
    }

    @Override
    public @NotNull Timer asThreadLocal() {
        return this;
    }
}
