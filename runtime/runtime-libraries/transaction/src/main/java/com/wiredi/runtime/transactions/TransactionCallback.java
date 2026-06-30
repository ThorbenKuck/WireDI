package com.wiredi.runtime.transactions;

/**
 * Callbacks that are notified when a transaction finishes.
 * <p>
 * The {@link AbstractTransaction} implementation guarantees the following order for a flush that leads to a
 * commit: first {@link #afterCommit()}, then {@link #afterFlush()}. For a flush that leads to a rollback:
 * {@link #afterRollback()} first, then {@link #afterFlush()}. Exceptions thrown by callback methods are caught and
 * ignored to avoid compromising the outcome of the transaction.
 * <p>
 * When callbacks are registered on non-flushable nested transactions, the default implementation delegates the
 * registration to the root so that they are executed exactly once when the outer transaction is flushed.
 * <p>
 * Example registration:
 * <pre>{@code
 * transaction.registerCallback(new TransactionCallback() {
 *   public void afterCommit() { audit("committed"); }
 *   public void afterRollback() { audit("rolled back"); }
 *   public void afterFlush() { metrics.increment("tx.flush"); }
 * });
 * }</pre>
 */
public interface TransactionCallback {

    void afterCommit();

    void afterRollback();

    void afterFlush();
}
