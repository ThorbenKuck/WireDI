package com.wiredi.runtime.async.barriers;

import org.jetbrains.annotations.NotNull;

/**
 * A {@link Barrier} whose state can be changed programmatically by calling code.
 * <p>
 * Implementations are expected to be thread-safe and to make {@link #open()} and {@link #close()} idempotent
 * (calling either method repeatedly should have no additional side effects after the first state transition).
 * <p>
 * Typical scenarios include unblocking threads once a service is ready, pausing/resuming workers during configuration
 * updates, or enabling features at runtime.
 */
public interface MutableBarrier extends Barrier {

    /**
     * Transitions the barrier to the closed state.
     * <p>
     * After closing, calls to {@link #traverse()} will block until the barrier is opened again (or fail if a timeout is used).
     * This operation should be idempotent: closing an already closed barrier should be a no-op.
     */
    void close();

    /**
     * Transitions the barrier to the open state.
     * <p>
     * After opening, calls to {@link #traverse()} return immediately. This operation should be idempotent: opening an
     * already open barrier should be a no-op.
     */
    void open();

    /**
     * Creates a new mutable barrier in the open state.
     *
     * @return a new open barrier
     */
    @NotNull
    static MutableBarrier opened() {
        final SemaphoreBarrier barrier = new SemaphoreBarrier();
        barrier.open();
        return barrier;
    }

    /**
     * Creates a new mutable barrier in the closed state.
     *
     * @return a new closed barrier
     */
    @NotNull
    static MutableBarrier closed() {
        final SemaphoreBarrier barrier = new SemaphoreBarrier();
        barrier.close();
        return barrier;
    }
}
