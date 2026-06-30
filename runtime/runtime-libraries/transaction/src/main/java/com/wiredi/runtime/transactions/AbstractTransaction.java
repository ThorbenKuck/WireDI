package com.wiredi.runtime.transactions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation of {@link Transaction} that encapsulates common behavior such as
 * parent propagation, rollback-only handling, callback management and idempotent flushing.
 * <p>
 * Nested transactions created from this class are typically not flushable so that only the root
 * of an isolation boundary actually commits or rolls back. This mirrors common database behavior
 * where only a real savepoint or outermost transaction can be completed.
 * <p>
 * Implementors provide the low-level {@link #commit()} and {@link #rollback()} operations and may
 * override {@link #flushable()} if the underlying platform supports savepoints.
 * When doing so, remember to respect nested transactions and check for the parent.
 */
public abstract class AbstractTransaction implements Transaction {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTransaction.class);
    @NotNull
    private final List<TransactionCallback> callbacks = new ArrayList<>();
    @Nullable
    private final Transaction parent;
    private boolean rollbackOnly = false;
    private boolean flushed = false;

    /**
     * Create a new transaction with the given parent. The rollbackOnly flag is propagated from the parent
     * so children inherit a rollback decision made earlier.
     */
    protected AbstractTransaction(@Nullable Transaction parent) {
        this.parent = parent;
        if (parent != null) {
            this.rollbackOnly = parent.isRollbackOnly();
        }
    }

    @Override
    public void registerCallback(@NotNull TransactionCallback callback) {
        if (parent != null && !flushable()) {
            // Bei nicht-flushbaren (verschachtelten) Transaktionen an den Parent delegieren,
            // sodass die Callbacks beim finalen Flush der Wurzel ausgeführt werden.
            parent.registerCallback(callback);
        } else {
            this.callbacks.add(callback);
        }
    }

    @Override
    public void setRollbackOnly() {
        rollbackOnly = true;
        if (parent != null) {
            parent.setRollbackOnly();
        }
    }

    @Override
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Whether this transaction can be flushed, i.e. whether {@link #flush()} will attempt to invoke {@link #commit()}
     * or {@link #rollback()}. The default implementation returns {@code true} for root transactions only.
     * Implementations that create database savepoints may override this to allow nested commits/rollbacks.
     *
     * @return true if flushing should result in a physical commit/rollback
     */
    public boolean flushable() {
        return isRoot();
    }

    @Override
    public @Nullable Transaction getParent() {
        return parent;
    }

    /**
     * Finalize the transaction. If marked rollback-only, {@link #rollback()} is called, otherwise {@link #commit()}.
     * Registered callbacks are executed afterwards. This method is idempotent and will no-op on non-flushable or
     * already-flushed transactions.
     */
    public void flush() {
        if (!flushable() || flushed) {
            return;
        }

        try {
            if (rollbackOnly) {
                if (rollback()) {
                    for (TransactionCallback callback : callbacks) {
                        try {
                            callback.afterRollback();
                        } catch (Throwable e) {
                            if (logger.isErrorEnabled()) {
                                logger.error("Error while executing afterRollback callback {}: {}", callback, e.getMessage(), e);
                            }
                        }
                    }
                }
            } else {
                if (commit()) {
                    for (TransactionCallback callback : callbacks) {
                        try {
                            callback.afterCommit();
                        } catch (Throwable e) {
                            if (logger.isErrorEnabled()) {
                                logger.error("Error while executing afterRollback callback {}: {}", callback, e.getMessage(), e);
                            }
                        }
                    }
                }
            }

            for (TransactionCallback callback : callbacks) {
                try {
                    callback.afterFlush();
                } catch (Throwable e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Error while executing afterRollback callback {}: {}", callback, e.getMessage(), e);
                    }
                }
            }
        } finally {
            flushed = true;
            callbacks.clear();
        }
    }

    /**
     * Commit the current transaction.
     *
     * @return whether the commit was performed (implementations may return false for non-flushable nested transactions)
     */
    protected abstract boolean commit();

    /**
     * Roll back the current transaction.
     *
     * @return whether the rollback was performed (implementations may return false for non-flushable nested transactions)
     */
    protected abstract boolean rollback();

    @Nullable
    protected Transaction parent() {
        return parent;
    }
}
