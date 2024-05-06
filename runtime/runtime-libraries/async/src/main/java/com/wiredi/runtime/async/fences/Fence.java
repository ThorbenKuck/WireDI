package com.wiredi.runtime.async.fences;

/**
 * A Fence is a class that holds another code fragment as its state, which allows for repeated invocations.
 * <p>
 * The problem that Fences aim to solve is synchronized, repeated invocations of code.
 * If you want to have control over synchronization of code executions, you can use these fences.
 * Depending on the type of fence, they ensure that code fragments are executed exactly once and synchronized.
 * <p>
 * The simples Fence is a {@code Fence.statement(() -> ...)}.
 * This Fence will execute the provided code fragment when {@link #pass()} is called.
 * <p>
 * A more elaborate Fence (like the local fence), will additionally ensure that the code executions happen in order
 * and mutually exclusive.
 * <p>
 * For example, let's say that we want to implement a DataAccessObject which has a buffer and a data source.
 * It uses a Fence to try and flush the buffer, like this:
 * <p>
 *
 * <pre><code>
 * public class DataAccessObject {
 *
 *     private final Datasource datasource;
 *     private final Buffer buffer;
 *     private final Fence flushBufferFence = Fence.statement(() -> {
 *          if (this.buffer.isFull()) {
 *              this.datasource.writeAll(this.buffer.content());
 *              this.buffer.clear();
 *          }
 *     });
 *
 *     public DataAccessObject(Datasource datasource) {
 *         this.datasource = datasource;
 *         buffer = new Buffer(datasource);
 *     }
 *
 *     public void handle(Entity entity) {
 *         buffer.append(entity);
 *         flushBufferFence.pass();
 *     }
 *
 *     public void flush() {
 *         flushBufferFence.pass();
 *     }
 * }
 * </code></pre>
 * <p>
 * For this example, let us imagine that {@code DataAccessObject} is invoked asynchronously and concurrent writes to the
 * sink can lead to ConcurrentModificationExceptions.
 * With every {@code append} to the buffer, the DataAccessObject will try to flush the buffer.
 * A simple {@code Fence.statement} will have potential issues with synchronizations.
 * It basically will "just invoke" the code statement when {@link #pass()} is called, which could mean that the writeAll
 * statement in the code sequence is called, given that multiple threads are executing the handle at the same time
 * However, fences understand more, they support two kinds of synchronizations: Local and Global.
 *
 * <h2>Local Synchronization</h2>
 * A locally synchronized fence will make sure that all method invocations to {@link #pass()} on the same instance
 * of this fence are synchronized.
 * If two separate Threads execute {@link #pass()} at the same time, the {@link Fence} will make sure that these
 * executions are mutually exclusive.
 * <p>
 * A locally synchronized fence can be created using {@code Fence.local(() -> this.buffer.flush())}
 *
 * <h2>Global Synchronizations</h2>
 * A globally synchronized fence will make sure that all invocations of {@link #pass()} on any thread are mutually
 * exclusive.
 * <p>
 * You can directly reference the {@link SynchronizedFence#global(Object, Runnable)}.
 * All global fences with the same {@code key} are synchronized.
 * <p>
 * A globally synchronized fence can be created using {@code Fence.global(() -> this.buffer.flush())}
 *
 * <h2>Considerations</h2>
 * - Fences should only be used if the code fragment is used repeatably. Exactly once invocations might make the code complex.
 * - Fences should only be used in concurrent environments. Otherwise, you can achieve the same behavior by just calling a method.
 *
 * @see SynchronizedFence
 * @see SimpleFence
 */
public interface Fence {

    /**
     * Creates a new, globally synchronized fence.
     * <p>
     * All calls to the globally synchronized fence will be synchronized between all processes that use it.
     * This is achieved by using the same lock instance for all global fences.
     *
     * @param runnable the code to execute when the fence is passed
     * @return a new instance of a Fence that is globally synchronized
     * @see SynchronizedFence#global(Object, Runnable)
     * @see SynchronizedFence
     */
    static Fence global(Runnable runnable) {
        return SynchronizedFence.global(Fence.class.getName(), runnable);
    }

    /**
     * Creates a new, locally synchronized fence.
     * <p>
     * Individual calls to the Fence are synchronized if the same instance is shared between multiple processes.
     *
     * @param runnable the code to execute when the fence is passed
     * @return a new instance of a Fence that is locally synchronized
     * @see SynchronizedFence#local(Runnable)
     * @see SynchronizedFence
     */
    static Fence local(Runnable runnable) {
        return SynchronizedFence.local(runnable);
    }

    /**
     * Will construct a simple fence statement, that is not synchronized.
     *
     * @param runnable the code to execute when the fence is passed
     * @return a new instance of a Fence
     * @see SimpleFence
     */
    static Fence statement(Runnable runnable) {
        return new SimpleFence(runnable);
    }

    /**
     * Pass the fence.
     * <p>
     * Invoking this method will cause the underlying process to be executed.
     * <p>
     * The execution of the underlying process is dependent on the implementation of the fence.
     *
     * @see SynchronizedFence#pass()
     * @see SimpleFence#pass()
     */
    void pass();

}
