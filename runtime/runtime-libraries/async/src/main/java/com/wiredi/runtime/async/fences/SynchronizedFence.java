package com.wiredi.runtime.async.fences;

import com.wiredi.logging.Logging;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link Fence} that enforces mutual exclusion using a {@link Lock}.
 * <p>
 * Two operating modes are supported:
 * <ul>
 *   <li><b>Local</b>: {@link #local(Runnable)} creates a fence with its own lock. Calls to {@link #pass()} on the same
 *       instance are serialized, while different instances operate independently.</li>
 *   <li><b>Global (keyed)</b>: {@link #global(Object, Runnable)} creates a fence that shares a lock with all other
 *       fences created with the same key. Calls to {@link #pass()} across those fences are mutually exclusive.</li>
 * </ul>
 *
 * Technical details:
 * <ul>
 *   <li>Locking is implemented with a {@link ReentrantLock} (non-fair by default). Reentrancy allows a thread that
 *       already holds the lock to call {@code pass()} again (directly or indirectly) without deadlocking.</li>
 *   <li>Global fences use a shared registry mapping keys to locks. Use stable, low-cardinality keys to avoid
 *       unbounded growth. Creating many distinct keys increases memory usage and reduces the benefit of coordination.</li>
 *   <li>Fairness is not guaranteed; threads may acquire the lock out of arrival order. If strict fairness is required,
 *       consider adapting the implementation to use a fair lock.</li>
 * </ul>
 *
 * How this differs from barriers:
 * <ul>
 *   <li>Synchronized fences serialize execution of a specific runnable; they do not model an open/closed gate.</li>
 *   <li>Barriers (see {@link com.wiredi.runtime.async.barriers.Barrier}) allow or block the caller based on state but do not
 *       inherently serialize a particular code fragment.</li>
 * </ul>
 *
 * Usage recommendations:
 * <ul>
 *   <li>Prefer {@link #local(Runnable)} when only a single instance needs serialization.</li>
 *   <li>Use {@link #global(Object, Runnable)} when multiple producers (possibly in different components) must not overlap.</li>
 *   <li>Avoid using highly dynamic keys for global fences; prefer well-defined constants to bound the key space.</li>
 *   <li>Keep the runnable short and non-blocking where possible to minimize lock contention.</li>
 * </ul>
 *
 * <h2>Examples</h2>
 *
 * <h3>1) Local serialization within a component</h3>
 * <pre><code>
 * class Aggregator {
 *   private final Batch batch = new Batch();
 *   private final SynchronizedFence fence = SynchronizedFence.local(() -> {
 *     if (batch.size() >= 1000) {
 *       writer.writeAll(batch.drain());
 *     }
 *   });
 *
 *   void onEvent(Event e) {
 *     batch.add(e);
 *     fence.pass(); // ensures writeAll does not overlap with itself for this instance
 *   }
 * }
 * </code></pre>
 *
 * <h3>2) Global coordination across components using a shared key</h3>
 * <pre><code>
 * // Component A
 * final SynchronizedFence a = SynchronizedFence.global("inventory-update", () -> inventory.apply(batchA.drain()));
 *
 * // Component B
 * final SynchronizedFence b = SynchronizedFence.global("inventory-update", () -> inventory.apply(batchB.drain()));
 *
 * // Calls will be serialized across both components under the same key:
 * a.pass();
 * b.pass();
 * </code></pre>
 */
public class SynchronizedFence implements Fence {

    private volatile Runnable runnable;
    private final Lock lock;
    private final Object key;

    private static final Logging logger = Logging.getInstance(SynchronizedFence.class);
    private static final Map<Object, Lock> locks = new HashMap<>();

    /**
     * Creates a locally synchronized fence backed by a new {@link ReentrantLock}.
     * <p>
     * Calls to {@link #pass()} on the same instance are mutually exclusive. Different instances are independent.
     *
     * @param runnable the code to execute when the fence is passed
     * @return a new locally synchronized fence
     */
    @NotNull
    public static SynchronizedFence local(@NotNull Runnable runnable) {
        return new SynchronizedFence(runnable);
    }

    /**
     * Creates a globally synchronized fence for the given key.
     * <p>
     * All fences created with the same {@code key} share the same lock and therefore serialize {@link #pass()} calls
     * across instances. Choose keys that are stable and limited in number to avoid unnecessary lock proliferation.
     *
     * @param key a stable object used to identify the global lock; equal keys share the same lock
     * @param runnable the code to execute when the fence is passed
     * @return a new globally synchronized fence
     */
    @NotNull
    public static SynchronizedFence global(@NotNull Object key, @NotNull Runnable runnable) {
        return new SynchronizedFence(runnable, locks.computeIfAbsent(key, it -> new ReentrantLock()), key);
    }

    /**
     * Constructs a local fence with a new {@link ReentrantLock}.
     *
     * @param runnable the code to execute when the fence is passed
     */
    public SynchronizedFence(@NotNull Runnable runnable) {
        this(runnable, new ReentrantLock(), SynchronizedFence.class);
    }

    /**
     * Constructs a synchronized fence with an explicit lock and key.
     * <p>
     * This constructor is useful for testing or advanced scenarios requiring a custom lock configuration.
     *
     * @param runnable the code to execute when the fence is passed
     * @param lock     the lock used to guard execution
     * @param key      an identifier used for logging and introspection
     */
    public SynchronizedFence(
            @NotNull Runnable runnable,
            @NotNull Lock lock,
            @NotNull Object key
    ) {
        this.runnable = runnable;
        this.lock = lock;
        this.key = key;
    }

    /**
     * Acquires the lock, executes the runnable, and releases the lock.
     * <p>
     * Behavior:
     * <ul>
     *   <li>Blocks until the lock is acquired.</li>
     *   <li>Always releases the lock in a {@code finally} block.</li>
     *   <li>Propagates any exception thrown by the runnable to the caller.</li>
     * </ul>
     */
    @Override
    public void pass() {
        try {
            lock.lock();
            logger.trace("Fence(" + key + ") passed");
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void set(@NotNull Runnable runnable) {
        try {
            lock.lock();
            this.runnable = runnable;
        } finally {
            lock.unlock();
        }
    }
}
