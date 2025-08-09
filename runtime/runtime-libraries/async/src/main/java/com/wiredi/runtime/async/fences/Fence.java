package com.wiredi.runtime.async.fences;

import org.jetbrains.annotations.NotNull;

/**
 * A Fence encapsulates a code fragment (Runnable) and defines how it is repeatedly executed under concurrency.
 * <p>
 * Conceptually, a fence is an execution policy for a statement: calling {@link #pass()} will execute the encapsulated
 * code, and the concrete fence implementation regulates synchronization and ordering guarantees between concurrent calls.
 * <p>
 * How this differs from barriers:
 * <ul>
 *   <li>Fences own the code to execute and orchestrate its invocation (including mutual exclusion in synchronized variants).</li>
 *   <li>Barriers (see {@link com.wiredi.runtime.async.barriers.Barrier}) act as pass/block gates for the calling thread but do not
 *       own or serialize a particular code fragment; they are typically used to pause/resume progress until a condition is met.</li>
 *   <li>Fences do not rely on an open/closed state. A call to {@code pass()} always attempts to execute the runnable
 *       immediately; synchronized implementations may block briefly to acquire a lock, but there is no concept of deferring
 *       execution until some separate "open" signal.</li>
 * </ul>
 * <p>
 * Variants:
 * <ul>
 *   <li>{@link #statement(Runnable)} returns a non-synchronized fence that simply runs the code when passed.</li>
 *   <li>{@link #local(Runnable)} returns a fence that provides mutual exclusion across calls to the same fence instance.</li>
 *   <li>{@link #global(Runnable)} returns a fence that provides mutual exclusion across all fences that share a common key.</li>
 * </ul>
 * <p>
 * Typical uses:
 * <ul>
 *   <li>Coalescing or serializing repeated background tasks (e.g., batching, periodic flush attempts) to avoid overlap.</li>
 *   <li>Ensuring a particular action is not executed concurrently by multiple threads.</li>
 *   <li>Providing a simple, explicit place to centralize safety checks around a critical code fragment.</li>
 * </ul>
 * <p>
 * Considerations:
 * <ul>
 *   <li>Fences are intended for repeated invocations. For one-off actions, call the method directly instead.</li>
 *   <li>Choose the appropriate synchronization level: prefer {@link #statement(Runnable)} for maximum throughput when overlap is safe,
 *       {@link #local(Runnable)} for per-instance serialization, and {@link #global(Runnable)} when multiple instances must not overlap.</li>
 *   <li>Global fences coordinate via a shared key space; use stable, low-cardinality keys and avoid excessive churn.</li>
 * </ul>
 *
 * <h2>Examples</h2>
 *
 * <h3>1) Local serialization of a batched flush</h3>
 * <pre><code>
 * class Dao {
 *   private final Datasource datasource;
 *   private final Buffer buffer = new Buffer();
 *   private final Fence flushFence = Fence.local(() -> {
 *     if (buffer.isFull()) {
 *       datasource.writeAll(buffer.drain());
 *     }
 *   });
 *
 *   Dao(Datasource datasource) {
 *     this.datasource = datasource;
 *   }
 *
 *   void handle(Event e) {
 *     buffer.append(e);
 *     flushFence.pass(); // ensures flush runs without overlapping with itself on this instance
 *   }
 *
 *   void flushNow() {
 *     flushFence.pass();
 *   }
 * }
 * </code></pre>
 *
 * <h3>2) Global coordination across components using a shared key</h3>
 * <pre><code>
 * // In component A
 * final SynchronizedFence saveFenceA =
 *     SynchronizedFence.global("orders-save", () -> orderRepository.save(batchA.drain()));
 *
 * // In component B
 * final SynchronizedFence saveFenceB =
 *     SynchronizedFence.global("orders-save", () -> orderRepository.save(batchB.drain()));
 *
 * // Anywhere they run, passing either fence will serialize write access under the same key:
 * saveFenceA.pass();
 * saveFenceB.pass();
 * </code></pre>
 *
 * <h3>3) Non-synchronized execution when overlap is safe</h3>
 * <pre><code>
 * final Fence emitMetrics = Fence.statement(() -> metrics.emitGauge("alive", 1));
 * emitMetrics.pass(); // executes immediately; concurrent calls may overlap
 * </code></pre>
 *
 * @see SimpleFence
 * @see SynchronizedFence
 * @see com.wiredi.runtime.async.barriers.Barrier
 */
public interface Fence {

    /**
     * Creates a new, globally synchronized fence.
     * <p>
     * Calls to {@link #pass()} on any fence created with the same global key are mutually exclusive across threads.
     * This is achieved by sharing a single lock per key. Use this when different components must serialize the same action.
     * <p>
     * Note: while passing the fence may block briefly to acquire the shared lock, there is no "open/closed" gating as with barriers.
     *
     * @param runnable the code to execute when the fence is passed; it should be idempotent or safe to run repeatedly
     * @return a new globally synchronized fence
     * @see SynchronizedFence#global(Object, Runnable)
     * @see SynchronizedFence
     */
    @NotNull
    static Fence global(@NotNull Runnable runnable) {
        return SynchronizedFence.global(Fence.class.getName(), runnable);
    }

    /**
     * Creates a new, locally synchronized fence.
     * <p>
     * Calls to {@link #pass()} on the same fence instance are mutually exclusive and will be executed in a serialized fashion.
     * Use this when a single component must ensure its own action does not overlap with itself.
     *
     * @param runnable the code to execute when the fence is passed; it should be idempotent or safe to run repeatedly
     * @return a new locally synchronized fence
     * @see SynchronizedFence#local(Runnable)
     * @see SynchronizedFence
     */
    @NotNull
    static Fence local(@NotNull Runnable runnable) {
        return SynchronizedFence.local(runnable);
    }

    /**
     * Constructs a simple, non-synchronized fence.
     * <p>
     * Each call to {@link #pass()} invokes the runnable immediately without any locking or serialization.
     * Use this when overlapping executions are safe or when external coordination already exists.
     *
     * @param runnable the code to execute when the fence is passed
     * @return a new non-synchronized fence
     * @see SimpleFence
     */
    @NotNull
    static Fence statement(@NotNull Runnable runnable) {
        return new SimpleFence(runnable);
    }

    /**
     * Executes the fence's code according to the implementation's synchronization policy.
     * <p>
     * - Non-synchronized fences run immediately.<br>
     * - Locally synchronized fences may block briefly while acquiring an instance-local lock.<br>
     * - Globally synchronized fences may block briefly while acquiring a shared lock for their key.<br>
     * Any exception thrown by the runnable is propagated to the caller.
     */
    void pass();

    /**
     * Updates the fence's underlying code.
     * <p>
     * Implementations should be thread-safe when replacing the runnable.
     *
     * @param runnable the new runnable to execute when the fence is passed
     */
    void set(@NotNull Runnable runnable);
}
