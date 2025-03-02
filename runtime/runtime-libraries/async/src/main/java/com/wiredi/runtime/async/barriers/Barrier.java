package com.wiredi.runtime.async.barriers;

import com.wiredi.runtime.async.AsyncBarrierException;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.function.Supplier;

public interface Barrier {

    /**
     * Returns true if the barrier allows traversal.
     * <p>
     * If true is returned, {@link #traverse()} should immediately return.
     *
     * @return true, if the barrier is open
     */
    boolean isOpen();

    /**
     * Returns true if the barrier is considered closed and does not allow traversal.
     * <p>
     * If true is returned, {@link #traverse()} should not return.
     *
     * @return true, if the barrier is open
     */
    boolean isClosed();

    void traverse() throws AsyncBarrierException;

    void traverse(@NotNull Duration duration) throws AsyncBarrierException;

    @NotNull
    static MutableBarrier create() {
        return new SemaphoreBarrier();
    }

    @NotNull
    static MutableBarrier opened() {
        final SemaphoreBarrier barrier = new SemaphoreBarrier();
        barrier.open();
        return barrier;
    }

    @NotNull
    static MutableBarrier closed() {
        final SemaphoreBarrier barrier = new SemaphoreBarrier();
        barrier.close();
        return barrier;
    }

    default boolean ifOpen(Runnable runnable) {
        if (isOpen()) {
            runnable.run();
            return true;
        }

        return false;
    }

    /**
     * Returns the value of the provided supplier, if the barrier is opened, otherwise null.
     *
     * @param supplier The supplier that generates the value if the barrier is opened.
     * @param <T>      the generic type of the value returned by the Supplier
     * @return the supplier value of the barrier is opened, or null.
     */
    default @Nullable <T> T ifOpen(@NotNull Supplier<@NotNull T> supplier) {
        if (isOpen()) {
            return supplier.get();
        }

        return null;
    }

    default boolean ifClosed(Runnable runnable) {
        if (isClosed()) {
            runnable.run();
            return true;
        }

        return false;
    }

    /**
     * Returns the value of the provided supplier, if the barrier is closed, otherwise null.
     *
     * @param supplier The supplier that generates the value if the barrier is closed.
     * @param <T>      the generic type of the value returned by the Supplier
     * @return the supplier value of the barrier is closed, or null.
     */
    default @Nullable <T> T ifClosed(@NotNull Supplier<@NotNull T> supplier) {
        if (isClosed()) {
            return supplier.get();
        }

        return null;
    }

    default void execute(@NotNull Runnable runnable) {
        traverse();
        runnable.run();
    }

    default void execute(
            @NotNull Duration duration,
            @NotNull Runnable runnable
    ) {
        traverse(duration);
        runnable.run();
    }

    default @NotNull <T> T get(@NotNull Supplier<T> supplier) {
        traverse();
        return supplier.get();
    }

    default @NotNull <T, E extends Throwable> T get(
            @NotNull Duration duration,
            @NotNull ThrowingSupplier<T, E> supplier
    ) throws E {
        traverse(duration);
        return supplier.get();
    }
}
