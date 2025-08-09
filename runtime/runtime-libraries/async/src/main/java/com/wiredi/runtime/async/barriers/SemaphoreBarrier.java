package com.wiredi.runtime.async.barriers;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.async.AsyncBarrierException;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A {@link MutableBarrier} implementation backed by a {@link Semaphore}.
 * <p>
 * Characteristics:
 * <ul>
 *   <li><b>Default state:</b> closed.</li>
 *   <li><b>Thread-safety:</b> safe for use by multiple threads concurrently.</li>
 *   <li><b>Idempotent transitions:</b> {@link #open()} and {@link #close()} are no-ops if the barrier is already in the
 *       requested state.</li>
 *   <li><b>Blocking semantics:</b> {@link #traverse()} blocks only when the barrier is closed at call time; when opened,
 *       queued waiters are released one-by-one. No strict fairness is guaranteed.</li>
 *   <li><b>Timeouts:</b> {@link #traverse(Duration)} waits up to the provided duration before failing.</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre><code>
 * final MutableBarrier barrier = SemaphoreBarrier.closed();
 *
 * // Worker thread
 * new Thread(() -> {
 *     barrier.traverse();
 *     // proceed once the barrier is opened
 * }).start();
 *
 * // Later: allow workers to proceed
 * barrier.open();
 * </code></pre>
 */
public final class SemaphoreBarrier implements MutableBarrier {

    private static final Logging logger = Logging.getInstance(SemaphoreBarrier.class);
    @NotNull
    private final Semaphore semaphore = new Semaphore(0);
    @NotNull
    private Supplier<@NotNull AsyncBarrierException> traversingFailedException = () -> new AsyncBarrierException("Could not traverse the barrier");
    private boolean isOpen = false;

    /**
     * Creates a new {@code SemaphoreBarrier} that is already open.
     *
     * @return a new instance in the open state
     */
    @NotNull
    public static SemaphoreBarrier opened() {
        @NotNull final SemaphoreBarrier barrier = new SemaphoreBarrier();
        barrier.open();
        return barrier;
    }

    /**
     * Creates a new {@code SemaphoreBarrier} that is closed.
     *
     * @return a new instance in the closed state
     */
    @NotNull
    public static SemaphoreBarrier closed() {
        @NotNull final SemaphoreBarrier barrier = new SemaphoreBarrier();
        barrier.close();
        return barrier;
    }

    /**
     * Returns a non-blocking snapshot of the open state.
     *
     * @return true if the barrier is observed as open
     */
    @Override
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Returns a non-blocking snapshot of the closed state.
     *
     * @return true if the barrier is observed as closed
     */
    @Override
    public boolean isClosed() {
        return !isOpen;
    }

    /**
     * Configures the message used when {@link #traverse(Duration)} fails to acquire within the timeout.
     *
     * @param message the message to include in the thrown {@link AsyncBarrierException}
     * @return this instance for chaining
     */
    public SemaphoreBarrier withTraversingFailedMessage(String message) {
        this.traversingFailedException = () -> new AsyncBarrierException(message);
        return this;
    }

    /**
     * Opens the barrier.
     * <p>
     * Idempotent: invoking {@code open()} on an already open barrier has no effect. Opening makes {@link #traverse()}
     * return immediately and releases at least one waiting thread; waiting threads continue to release one-by-one as
     * they re-signal internally. Newly arriving threads will not block while the barrier remains open.
     */
    @Override
    public void open() {
        if (isOpen) {
            return;
        }
        logger.trace("Opening Barrier");
        isOpen = true;
        semaphore.release();
    }

    /**
     * Closes the barrier.
     * <p>
     * Idempotent: invoking {@code close()} on an already closed barrier has no effect. Closing ensures subsequent
     * calls to {@link #traverse()} will block (or time out). This operation may internally acquire a permit to
     * represent the closed state; if the current thread is interrupted while doing so, an {@link AsyncBarrierException}
     * is thrown.
     */
    @Override
    public void close() {
        if (!isOpen) {
            return;
        }

        logger.trace("Closing Barrier");
        isOpen = false;
        try {
            semaphore.acquire();
        } catch (@NotNull final InterruptedException e) {
            throw new AsyncBarrierException(e);
        }
    }

    /**
     * Blocks the calling thread until the barrier is open, then returns.
     * <p>
     * If the barrier is already open, this is a no-op. If the thread is interrupted while waiting, the interruption
     * is translated to an {@link AsyncBarrierException}.
     *
     * @throws AsyncBarrierException if interrupted while waiting
     */
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

    /**
     * Attempts to traverse the barrier, waiting up to the given duration.
     * <p>
     * If the barrier does not open in time, an {@link AsyncBarrierException} is thrown (using the message configured
     * via {@link #withTraversingFailedMessage(String)}). If the thread is interrupted while waiting, the interruption
     * is translated to an {@link AsyncBarrierException}.
     *
     * @param duration the maximum time to wait
     * @throws AsyncBarrierException on timeout or interruption
     */
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

    /**
     * Returns a string representation containing the open/closed state.
     */
    @Override
    public String toString() {
        return "SemaphoreBarrier{" +
                "open=" + isOpen +
                '}';
    }
}
