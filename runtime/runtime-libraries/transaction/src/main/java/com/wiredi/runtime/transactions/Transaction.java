package com.wiredi.runtime.transactions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A transactional context that can be nested and flushed.
 * A Transaction represents a unit of work that can either be committed or rolled back when {@link #flush()} is called.
 * Implementations decide whether a particular instance is flushable (typically only the root of an isolation level).
 * <p>
 * WireDI treats nested transactions as delegating callback registration and rollback-only state to the root unless
 * the nested instance represents a real savepoint or isolation boundary.
 * A call to {@link #flush()} performs either a commit or a rollback depending on {@link #isRollbackOnly()} and then
 * invokes all registered callbacks.
 * The {@link TransactionStateManager} ensures that {@code flush()} is invoked and maintains the current transaction
 * pointer per thread.
 * <p>
 * Example: registering callbacks and marking rollbackOnly
 * <pre>{@code
 * manager.run(status -> {
 *     status.registerCallback(new TransactionCallback() {
 *         public void afterCommit() { System.out.println("committed"); }
 *         public void afterRollback() { System.out.println("rolled back"); }
 *         public void afterFlush() { System.out.println("finished"); }
 *     });
 *
 *     if (someCondition) {
 *         status.setRollbackOnly();
 *     }
 * });
 * }</pre>
 */
public interface Transaction extends AutoCloseable {

    static Transaction noOp() {
        return new NoOpTransaction();
    }

    /**
     * Create a nested transactional context. Implementations may return a child that is not flushable
     * unless the underlying platform creates a savepoint. The default {@code SimpleTransaction} creates
     * non-flushable children so only the root flushes.
     *
     * @return the nested transaction
     */
    @NotNull
    Transaction nest();

    /**
     * Register a callback that is invoked around commit/rollback and after flush. Implementations may delegate
     * callback registration to the root transaction if this instance is not flushable.
     */
    void registerCallback(@NotNull TransactionCallback callback);

    /**
     * Mark this transaction (and implicitly its root) as rollback-only. After this call, a subsequent {@link #flush()}
     * must roll back instead of committing.
     */
    void setRollbackOnly();

    /**
     * Whether this transaction has been marked as rollback-only.
     */
    boolean isRollbackOnly();

    /**
     * Whether this transaction has no parent.
     */
    boolean isRoot();

    /**
     * The parent transaction if this is a nested transaction; otherwise {@code null}.
     */
    @Nullable
    Transaction getParent();

    /**
     * Default throwable handling used by managers: mark rollback-only. Implementations may override to translate
     * exceptions or collect metadata, but should always ensure the transaction is rolled back.
     */
    default void handleThrowable(Throwable throwable) {
        setRollbackOnly();
    }

    /**
     * AutoCloseable support. Delegates to {@link #flush()}.
     */
    default void close() {
        flush();
    }

    /**
     * Complete the transaction by committing or rolling back depending on {@link #isRollbackOnly()}.
     * Implementations should be idempotent and may ignore calls on non-flushable transactions.
     */
    void flush();

}
