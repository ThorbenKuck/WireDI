package com.wiredi.runtime.transactions.simple;

import com.wiredi.runtime.transactions.AbstractTransaction;
import com.wiredi.runtime.transactions.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Minimal {@link Transaction} implementation used for tests, demos and as a reference.
 * <p>
 * This transaction does not interact with external resources. Committing or rolling back only
 * affects in-memory counters in tests and registered callbacks. Nested instances are created via
 * {@link #nest()} and are not flushable; only the root transaction performs a physical flush by
 * design, aligning with typical behavior where savepoints are absent.
 * <p>
 * Example usage with a basic TransactionFactory:
 * <pre>{@code
 * TransactionFactory factory = new TransactionFactory() {
 *   public Transaction createNewTransaction(@NotNull TransactionProperties p) { return new SimpleTransaction(); }
 *   public Transaction createNestedTransaction(@NotNull Transaction parent, @NotNull TransactionProperties p) { return new SimpleTransaction(parent); }
 * };
 * TransactionManager tm = new PlatformTransactionManager(factory);
 * tm.run(status -> {
 *   // work
 * });
 * }</pre>
 */
public class SimpleTransaction extends AbstractTransaction {

    /**
     * Create a nested simple transaction. Children inherit rollback-only state and delegate callbacks to the root.
     */
    protected SimpleTransaction(@Nullable Transaction parent) {
        super(parent);
    }

    /**
     * Create a root simple transaction.
     */
    protected SimpleTransaction() {
        super(null);
    }

    /**
     * Simulate committing this transaction. Since no external resources are touched, the method returns {@code true}
     * to indicate the commit would have been performed.
     */
    @Override
    protected boolean commit() {
        return true;
    }

    /**
     * Simulate rolling back this transaction. No external resources are involved; returns {@code true} to signal
     * the rollback would have been performed.
     */
    @Override
    protected boolean rollback() {
        return true;
    }

    /**
     * Create a non-flushable child transaction linked to this instance.
     */
    @Override
    public @NotNull Transaction nest() {
        return new SimpleTransaction(this);
    }
}
