package com.wiredi.runtime.async;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A barrier is a simple synchronization mechanism.
 * <p>
 * It functions similar to a physical barrier, which needs to be traversed.
 * <p>
 * A barrier can be either open or closed.
 * Traversing a closed Barrier will suspend the traversing {@link Thread} until the barrier is opened.
 * <p>
 * All queued threads will be released in order they tried to traverse the Barrier.
 * Any new {@link Thread} added to the barrier as long as it remains opened will not be suspended, even
 * if there are still suspended Threads that are waiting to be executed.
 * <p>
 * A Barrier functionally differs from a Lock in that it is not meant to synchronize access to a resource.
 * Instead, it allows for operational suspending until a certain condition is met.
 * <p>
 * <h2>Example</h2>
 * <p>
 * The following code illustrates the basic usage of a Barrier
 *
 * <pre><code>
 * class MyExample {
 *    private final Barrier barrier = Barrier.closed();
 *
 *    public void waitUntilPublished() {
 *        barrier.traverse();
 *        // My logic when the barrier is done
 *    }
 *
 *    public void publish() {
 *        barrier.open();
 *    }
 * }
 * </code></pre>
 * <p>
 * The thread that executed `waitUntilPublished` will be suspended until another thread called `publish`.
 */
public final class Barrier {

    private static final Logging logger = Logging.getInstance(Barrier.class);
    @NotNull
    private final Semaphore semaphore = new Semaphore(0);
    @NotNull
    private Supplier<@NotNull AsyncBarrierException> traversingFailedException = () -> new AsyncBarrierException("Could not traverse the barrier");
    private boolean isOpen = false;

    @NotNull
    public static Barrier opened() {
        @NotNull final Barrier barrier = new Barrier();
        barrier.open();
        return barrier;
    }

    @NotNull
    public static Barrier closed() {
        @NotNull final Barrier barrier = new Barrier();
        barrier.close();
        return barrier;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public boolean isClosed() {
        return !isOpen;
    }

    public Barrier withTraversingFailedMessage(String message) {
        this.traversingFailedException = () -> new AsyncBarrierException(message);
        return this;
    }

    public void open() {
        if (isOpen) {
            return;
        }
        logger.debug("Opening Barrier");
        isOpen = true;
        semaphore.release();
    }

    public boolean ifOpen(Runnable runnable) {
        if (isOpen) {
            runnable.run();
            return true;
        }

        return false;
    }

    /**
     * Returns the value of the provided supplier, if the barrier is opened, otherwise null.
     *
     * @param supplier The supplier that generates the value if the barrier is opened.
     * @param <T> the generic type of the value returned by the Supplier
     * @return the supplier value of the barrier is opened, or null.
     */
    @Nullable
    public <T> T ifOpen(@NotNull Supplier<@NotNull T> supplier) {
        if (isOpen) {
            return supplier.get();
        }

        return null;
    }

    public void close() {
        if (!isOpen) {
            return;
        }

        logger.debug("Closing Barrier");
        isOpen = false;
        try {
            semaphore.acquire();
        } catch (@NotNull final InterruptedException e) {
            throw new AsyncBarrierException(e);
        }
    }

    public boolean ifClosed(Runnable runnable) {
        if (!isOpen) {
            runnable.run();
            return true;
        }

        return false;
    }

    /**
     * Returns the value of the provided supplier, if the barrier is closed, otherwise null.
     *
     * @param supplier The supplier that generates the value if the barrier is closed.
     * @param <T> the generic type of the value returned by the Supplier
     * @return the supplier value of the barrier is closed, or null.
     */
    @Nullable
    public <T> T ifClosed(@NotNull Supplier<@NotNull T> supplier) {
        if (!isOpen) {
            return supplier.get();
        }

        return null;
    }

    public void traverse() throws AsyncBarrierException {
        if (!isOpen) {
            try {
                semaphore.acquire();
                semaphore.release();
            } catch (@NotNull final InterruptedException e) {
                throw new AsyncBarrierException(e);
            }
        }
    }

    public void traverse(@NotNull final Duration duration) throws AsyncBarrierException {
        if (!isOpen) {
            try {
                if (!semaphore.tryAcquire(duration.toNanos(), TimeUnit.NANOSECONDS)) {
                    throw traversingFailedException.get();
                }
                semaphore.release();
            } catch (@NotNull final InterruptedException e) {
                throw new AsyncBarrierException(e);
            }
        }
    }

    public void execute(@NotNull final Runnable runnable) {
        traverse();
        runnable.run();
    }

    public void execute(
            @NotNull final Duration duration,
            @NotNull final Runnable runnable
    ) {
        traverse(duration);
        runnable.run();
    }

    @NotNull
    public <T> T get(@NotNull final Supplier<T> supplier) {
        traverse();
        return supplier.get();
    }

    @NotNull
    public <T, E extends Throwable> T get(
            @NotNull final Duration duration,
            @NotNull final ThrowingSupplier<T, E> supplier
    ) throws E {
        traverse(duration);
        return supplier.get();
    }
}
