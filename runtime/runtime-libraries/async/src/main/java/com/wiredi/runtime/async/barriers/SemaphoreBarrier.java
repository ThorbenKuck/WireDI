package com.wiredi.runtime.async.barriers;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.async.AsyncBarrierException;
import org.jetbrains.annotations.NotNull;

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
public final class SemaphoreBarrier implements MuitableBarrier {

    private static final Logging logger = Logging.getInstance(SemaphoreBarrier.class);
    @NotNull
    private final Semaphore semaphore = new Semaphore(0);
    @NotNull
    private Supplier<@NotNull AsyncBarrierException> traversingFailedException = () -> new AsyncBarrierException("Could not traverse the barrier");
    private boolean isOpen = false;

    @NotNull
    public static SemaphoreBarrier opened() {
        @NotNull final SemaphoreBarrier barrier = new SemaphoreBarrier();
        barrier.open();
        return barrier;
    }

    @NotNull
    public static SemaphoreBarrier closed() {
        @NotNull final SemaphoreBarrier barrier = new SemaphoreBarrier();
        barrier.close();
        return barrier;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public boolean isClosed() {
        return !isOpen;
    }

    public SemaphoreBarrier withTraversingFailedMessage(String message) {
        this.traversingFailedException = () -> new AsyncBarrierException(message);
        return this;
    }

    @Override
    public void open() {
        if (isOpen) {
            return;
        }
        logger.debug("Opening Barrier");
        isOpen = true;
        semaphore.release();
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public String toString() {
        return "SemaphoreBarrier{" +
                "open=" + isOpen +
                '}';
    }
}
