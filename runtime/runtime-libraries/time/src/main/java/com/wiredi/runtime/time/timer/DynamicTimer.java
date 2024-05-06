package com.wiredi.runtime.time.timer;

import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.time.timer.exceptions.TimerAlreadyStartedException;
import com.wiredi.runtime.time.timer.exceptions.TimerNotStartedException;
import com.wiredi.runtime.time.timer.interpreter.TimeContext;
import org.jetbrains.annotations.NotNull;

/**
 * A simple timer implementation, storing the time as the timer was started.
 * <p>
 * The time is given by the {@link TimeContext}.
 */
public class DynamicTimer implements Timer {

    private final TimeContext context;
    private long start = -1;

    public DynamicTimer(TimeContext context) {
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStarted() {
        return start > -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        if (isStarted()) {
            throw new TimerAlreadyStartedException("Timer is already running");
        }

        start = context.current();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Timed stop() {
        long elapsed = context.current() - start;
        if (!isStarted()) {
            throw new TimerNotStartedException("Timer was not started");
        }
        start = -1;
        return new Timed(context.toNanos(elapsed));
    }

    @Override
    public @NotNull Timer asThreadLocal() {
        return new ThreadLocalTimer(context);
    }

    public ThreadLocalTimer threadLocal() {
        return new ThreadLocalTimer(context);
    }
}
