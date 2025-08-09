package com.wiredi.runtime.async.barriers;

import com.wiredi.runtime.async.AsyncBarrierException;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * A lightweight, thread-safe gate that controls whether code is allowed to proceed.
 * <p>
 * A barrier can be in one of two logical states:
 * <ul>
 *   <li><b>Open</b>: calls to {@link #traverse()} return immediately.</li>
 *   <li><b>Closed</b>: calls to {@link #traverse()} block the calling thread (or fail when a timeout is used).</li>
 * </ul>
 * This abstraction is useful to defer or pause execution until some condition is met (e.g., readiness, configuration
 * reload, feature enablement, maintenance windows) without introducing ownership of a shared resource as with locks.
 * <p>
 * Concurrency semantics:
 * <ul>
 *   <li>{@link #isOpen()} and {@link #isClosed()} are non-blocking snapshots. They are helpful for fast-path checks,
 *       but should not be used to establish correctness. To enforce the gate, call {@link #traverse()}.</li>
 *   <li>{@link #traverse()} establishes the required synchronization for passing the gate. It blocks only when the
 *       barrier is closed at the time of the call.</li>
 *   <li>Timeout-based traversal via {@link #traverse(Duration)} allows bounded waiting.</li>
 * </ul>
 * <p>
 * Convenience methods are provided to conditionally run code or obtain values when the barrier is open/closed, and to
 * execute suppliers after passing through the barrier.
 * <p>
 * Implementations must be safe for concurrent use by multiple threads.
 */
public interface Barrier {

    /**
     * Returns true if the barrier currently allows traversal.
     * <p>
     * This is a non-blocking, best-effort snapshot that may change immediately due to concurrent state transitions.
     * To enforce gating semantics, prefer {@link #traverse()}.
     *
     * @return true if the barrier is open at the time of the call
     */
    boolean isOpen();

    /**
     * Returns true if the barrier is currently considered closed and does not allow traversal.
     * <p>
     * This is a non-blocking, best-effort snapshot that may change immediately due to concurrent state transitions.
     * To enforce gating semantics, prefer {@link #traverse()}.
     *
     * @return true if the barrier is closed at the time of the call
     */
    boolean isClosed();

    /**
     * Blocks the current thread until the barrier is open, then returns.
     * <p>
     * If the barrier is already open at the time of the call, this method returns immediately. Otherwise, it blocks
     * until the barrier transitions to open.
     *
     * @throws AsyncBarrierException if traversal cannot be completed, for example due to an interruption while waiting
     *                               or implementation-specific failure. See concrete implementations for details about
     *                               how interruptions are handled.
     */
    void traverse() throws AsyncBarrierException;

    /**
     * Attempts to traverse the barrier, waiting up to the specified duration.
     * <p>
     * If the barrier becomes open within the given time, the method returns; otherwise it throws an
     * {@link AsyncBarrierException}. Non-positive durations generally lead to an immediate check without waiting.
     *
     * @param duration the maximum time to wait to traverse the barrier
     * @throws AsyncBarrierException if the barrier cannot be traversed within the specified duration, if the thread is
     *                               interrupted while waiting, or on implementation-specific failure. See implementations
     *                               for details about the exact exception cause semantics.
     */
    void traverse(@NotNull Duration duration) throws AsyncBarrierException;

    /**
     * Creates a new mutable barrier instance in its default state.
     * <p>
     * The default state is implementation-specific but typically starts out closed so that callers explicitly decide
     * when to open it.
     *
     * @return a new mutable barrier
     */
    @NotNull
    static MutableBarrier create() {
        return new SemaphoreBarrier();
    }

    /**
     * Creates a new mutable barrier that is already open.
     *
     * @return a new open barrier
     */
    @NotNull
    static MutableBarrier opened() {
        return MutableBarrier.opened();
    }

    /**
     * Creates a new mutable barrier that is closed.
     *
     * @return a new closed barrier
     */
    @NotNull
    static MutableBarrier closed() {
        return MutableBarrier.closed();
    }

    /**
     * Executes the provided runnable only if the barrier is currently open.
     * <p>
     * This method does not block and does not establish a synchronization point beyond the conditional check.
     *
     * @param runnable the action to run when the barrier is open
     * @return true if the runnable was executed; false otherwise
     */
    default boolean ifOpen(Runnable runnable) {
        if (isOpen()) {
            runnable.run();
            return true;
        }

        return false;
    }

    /**
     * Returns the value of the provided supplier if the barrier is currently open; otherwise returns null.
     * <p>
     * The supplier is only evaluated when the barrier is open at the time of the call. This method does not block.
     *
     * @param supplier the supplier to invoke when the barrier is open
     * @param <T>      the value type
     * @return the supplied value when open; null otherwise
     */
    default @Nullable <T> T ifOpen(@NotNull Supplier<@NotNull T> supplier) {
        if (isOpen()) {
            return supplier.get();
        }

        return null;
    }

    /**
     * Executes the provided runnable only if the barrier is currently closed.
     * <p>
     * This method does not block and does not establish a synchronization point beyond the conditional check.
     *
     * @param runnable the action to run when the barrier is closed
     * @return true if the runnable was executed; false otherwise
     */
    default boolean ifClosed(Runnable runnable) {
        if (isClosed()) {
            runnable.run();
            return true;
        }

        return false;
    }

    /**
     * Returns the value of the provided supplier if the barrier is currently closed; otherwise returns null.
     * <p>
     * The supplier is only evaluated when the barrier is closed at the time of the call. This method does not block.
     *
     * @param supplier the supplier to invoke when the barrier is closed
     * @param <T>      the value type
     * @return the supplied value when closed; null otherwise
     */
    default @Nullable <T> T ifClosed(@NotNull Supplier<@NotNull T> supplier) {
        if (isClosed()) {
            return supplier.get();
        }

        return null;
    }

    /**
     * Traverses the barrier and then executes the given runnable.
     * <p>
     * If traversal fails, the runnable is not executed.
     *
     * @param runnable the action to execute after successful traversal
     * @throws AsyncBarrierException if traversal fails
     */
    default void execute(@NotNull Runnable runnable) {
        traverse();
        runnable.run();
    }

    /**
     * Attempts to traverse the barrier within the given duration and then executes the given runnable.
     * <p>
     * If traversal fails (including timeout), the runnable is not executed.
     *
     * @param duration the maximum time to wait for traversal
     * @param runnable the action to execute after successful traversal
     * @throws AsyncBarrierException if traversal fails or times out
     */
    default void execute(
            @NotNull Duration duration,
            @NotNull Runnable runnable
    ) {
        traverse(duration);
        runnable.run();
    }

    /**
     * Traverses the barrier and then obtains a value from the given supplier.
     *
     * @param supplier the supplier to invoke after successful traversal
     * @param <T>      the value type
     * @return the supplied value
     * @throws AsyncBarrierException if traversal fails
     */
    default @NotNull <T> T get(@NotNull Supplier<T> supplier) {
        traverse();
        return supplier.get();
    }

    /**
     * Attempts to traverse the barrier within the given duration and then obtains a value from the given supplier.
     *
     * @param duration the maximum time to wait for traversal
     * @param supplier the supplier to invoke after successful traversal
     * @param <T>      the value type
     * @param <E>      the checked exception type that the supplier may throw
     * @return the supplied value
     * @throws AsyncBarrierException if traversal fails or times out
     * @throws E                     if the supplier throws an exception
     */
    default @NotNull <T, E extends Throwable> T get(
            @NotNull Duration duration,
            @NotNull ThrowingSupplier<T, E> supplier
    ) throws E {
        traverse(duration);
        return supplier.get();
    }
}
