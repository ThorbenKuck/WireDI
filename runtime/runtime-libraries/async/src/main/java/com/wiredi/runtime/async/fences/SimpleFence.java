package com.wiredi.runtime.async.fences;

import com.wiredi.logging.Logging;
import org.jetbrains.annotations.NotNull;

/**
 * A non-synchronized {@link Fence} that directly executes its runnable when {@link #pass()} is invoked.
 * <p>
 * Characteristics:
 * <ul>
 *   <li>No mutual exclusion: multiple concurrent calls to {@code pass()} may execute the runnable concurrently.</li>
 *   <li>Zero coordination overhead: best option when overlapping executions are safe or coordination is handled elsewhere.</li>
 *   <li>Error propagation: exceptions thrown by the runnable are propagated to the caller.</li>
 * </ul>
 * How this differs from barriers:
 * <ul>
 *   <li>There is no notion of waiting for an "open" condition; {@code pass()} immediately executes the runnable.</li>
 *   <li>Barriers gate thread progress; this class orchestrates execution of a specific code fragment without gating.</li>
 * </ul>
 *
 * Thread-safety considerations:
 * <ul>
 *   <li>The runnable must be thread-safe if {@code pass()} can be called concurrently.</li>
 *   <li>If you need mutual exclusion, prefer a synchronized fence such as {@link SynchronizedFence} (via {@link Fence#local(Runnable)}).</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre><code>
 * // Periodically refresh a cache without serialization needs
 * final SimpleFence refreshFence = new SimpleFence(() -> cache.refresh());
 *
 * scheduler.scheduleAtFixedRate(refreshFence::pass, 0, 5, TimeUnit.MINUTES);
 *
 * // Or invoke directly when an event arrives
 * refreshFence.pass();
 * </code></pre>
 */
public class SimpleFence implements Fence {

    @NotNull
    private volatile Runnable runnable;
    private static final Logging logger = Logging.getInstance(SimpleFence.class);

    /**
     * Creates a new {@code SimpleFence}.
     *
     * @param runnable the code to execute each time {@link #pass()} is invoked
     */
    public SimpleFence(@NotNull Runnable runnable) {
        this.runnable = runnable;
    }

    /**
     * Executes the wrapped runnable immediately.
     * <p>
     * A trace log entry is emitted before executing the runnable. Any exception thrown by the runnable is propagated
     * to the caller.
     */
    @Override
    public void pass() {
        logger.trace(() -> "Fence passed");
        Runnable runnable = this.runnable;
        runnable.run();
    }

    @Override
    public void set(@NotNull Runnable runnable) {
        this.runnable = runnable;
    }
}
