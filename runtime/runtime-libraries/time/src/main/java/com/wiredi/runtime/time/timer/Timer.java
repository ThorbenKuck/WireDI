package com.wiredi.runtime.time.timer;

import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.time.timer.exceptions.TimerAlreadyStartedException;
import com.wiredi.runtime.time.timer.exceptions.TimerNotStartedException;
import com.wiredi.runtime.time.timer.interpreter.TimeContext;
import org.jetbrains.annotations.NotNull;

/**
 * A timer is a stateful factory for constructing {@link Timed} instances.
 * <p>
 * A timer is switching between two states: started and stopped.
 * Any implementation must follow these rules:
 * <ul>
 *     <li>A stopped timer can be started</li>
 *     <li>A started timer can be stopped and construct a new {@link Timed} instance</li>
 *     <li>If a stopped timer is stopped again, an {@link TimerNotStartedException} must be raised</li>
 *     <li>If a started timer is started again, an {@link TimerAlreadyStartedException} must be raised</li>
 *     <li>A new instance of a timer is always stopped</li>
 * </ul>
 * <p>
 * Additionally, the {@link Timed} constructed by the {@link #stop()} method should be no more than 0.2% larger than
 * the actually elapsed time.
 * This particular also applies to not warmed up instances of the JVM.
 *
 * @see DynamicTimer
 * @see ThreadLocalTimer
 * @see Timed
 */
public interface Timer {

    /**
     * Constructs a timer with nanosecond precision.
     * <p>
     * The timer will be more precise, but potentially slower as it uses {@link System#nanoTime()}
     *
     * @return a new timer with nanosecond precision.
     */
    @NotNull
    static DynamicTimer nano() {
        return new DynamicTimer(TimeContext.NANOS);
    }

    @NotNull
    static DynamicTimer milli() {
        return new DynamicTimer(TimeContext.MILLIS);
    }

    @NotNull
    static DynamicTimer of(@NotNull TimeContext context) {
        return new DynamicTimer(context);
    }

    @NotNull
    static ThreadLocalTimer threadLocal() {
        return nano().threadLocal();
    }

    @NotNull
    static ThreadLocalTimer threadLocalNano() {
        return nano().threadLocal();
    }

    @NotNull
    static ThreadLocalTimer threadLocalMillis() {
        return milli().threadLocal();
    }

    @NotNull
    static ThreadLocalTimer threadLocal(@NotNull TimeContext context) {
        return new ThreadLocalTimer(context);
    }

    /**
     * Whether the timer is started or not.
     *
     * @return true, if the timer ist started, or false otherwise.
     */
    boolean isStarted();

    /**
     * Start the timer.
     * <p>
     * A timer can only be started if it is stopped.
     * If the timer is not stopped, a {@link TimerAlreadyStartedException} is raised.
     *
     * @throws TimerAlreadyStartedException if the timer is already running
     */
    void start() throws TimerAlreadyStartedException;

    /**
     * Stop the timer.
     * <p>
     * A timer can only be stopped if it was started previously.
     * If the timer is not started, a {@link TimerNotStartedException} is raised.
     *
     * @return a new {@link Timed} instance containing the elapsed nanoseconds
     * @throws TimerNotStartedException if the timer is not yet started
     */
    @NotNull
    Timed stop() throws TimerNotStartedException;

    @NotNull
    Timer asThreadLocal();
}
