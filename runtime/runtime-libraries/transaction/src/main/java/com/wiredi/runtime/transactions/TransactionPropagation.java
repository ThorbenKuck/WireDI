package com.wiredi.runtime.transactions;

import com.wiredi.runtime.lang.ThrowingFunction;
import com.wiredi.runtime.transactions.exception.MissingTransactionException;

/**
 * Propagation semantics describe how a new transactional block relates to an already active one.
 * <p>
 * When no transaction is active, a new root is created by delegating to the configured {@link TransactionFactory}.
 * When a transaction is active, behavior depends on the chosen semantic: joining the current context as a nested
 * transaction, demanding that no transaction is present, requiring an existing one, or always starting a new root.
 * <p>
 * The default behavior in WireDI is SUPPORTED, which creates a nested child when a transaction is present and a
 * new root otherwise. Nested children are typically not flushable unless the backend implements savepoints; they
 * therefore delegate callbacks and completion to the outer root. REQUIRES_NEW always creates an independent root
 * that flushes independently. NEVER forbids nesting and fails fast when a transaction is present. REQUIRED expects
 * a transaction to be already open and, in the current implementation, nests an additional level by passing
 * {@code current.nest()} into the factory.
 */
public enum TransactionPropagation {
    /**
     * Use any currently bound transaction or create a new one if none is present.
     */
    CREATE_IF_MISSING {
        @Override
        public <R, E extends Throwable, TX extends Transaction> R applyTo(
                TransactionProperties properties,
                TransactionFactory<TX> factory,
                TransactionManager<TX> manager,
                ThrowingFunction<TX, R, E> callback
        ) throws E {
            TX transaction = manager.currentTransaction();
            if (transaction == null) {
                transaction = factory.createNewTransaction(properties);
            }

            return callback.apply(transaction);
        }
    },
    /**
     * Uses a transaction if one is present, otherwise runs without a transaction.
     */
    USE_IF_AVAILABLE {
        @Override
        public <R, E extends Throwable, TX extends Transaction> R applyTo(
                TransactionProperties properties,
                TransactionFactory<TX> factory,
                TransactionManager<TX> manager,
                ThrowingFunction<TX, R, E> callback
        ) throws E {
            TX transaction = manager.currentTransaction();
            return callback.apply(transaction);
        }
    },
    /**
     * Requires an active transaction or throws an exception if none is present.
     */
    REQUIRE_EXISTING {
        @Override
        public <R, E extends Throwable, TX extends Transaction> R applyTo(
                TransactionProperties properties,
                TransactionFactory<TX> factory,
                TransactionManager<TX> manager,
                ThrowingFunction<TX, R, E> callback
        ) throws E {
            TX transaction = manager.currentTransaction();
            if (transaction == null) {
                throw new MissingTransactionException("Cannot execute transactional code without an active transaction.");
            }

            return callback.apply(transaction);
        }
    },
    /**
     * Creates a new transaction, suspending the current one if present.
     */
    REQUIRES_NEW {
        @Override
        public <R, E extends Throwable, TX extends Transaction> R applyTo(
                TransactionProperties properties,
                TransactionFactory<TX> factory,
                TransactionManager<TX> manager,
                ThrowingFunction<TX, R, E> callback
        ) throws E {
            TX transaction = manager.currentTransaction();

            if (transaction != null) {
                try (var ignored = manager.suspend()) {
                    return callback.apply(factory.createNewTransaction(properties));
                }
            } else {
                return callback.apply(factory.createNewTransaction(properties));
            }
        }
    },
    /**
     * Does not use a transaction and suspends any active transaction.
     */
    SUSPEND_TRANSACTIONS {
        @Override
        public <R, E extends Throwable, TX extends Transaction> R applyTo(
                TransactionProperties properties,
                TransactionFactory<TX> factory,
                TransactionManager<TX> manager,
                ThrowingFunction<TX, R, E> callback
        ) throws E {
            TX transaction = manager.currentTransaction();

            if (transaction != null) {
                try (var ignored = manager.suspend()) {
                    return callback.apply(null);
                }
            } else {
                return callback.apply(null);
            }
        }
    },
    /**
     * Does not use a transaction and fails if a transaction is present.
     */
    NOT_IN_TRANSACTION {
        @Override
        public <R, E extends Throwable, TX extends Transaction> R applyTo(
                TransactionProperties properties,
                TransactionFactory<TX> factory,
                TransactionManager<TX> manager,
                ThrowingFunction<TX, R, E> callback
        ) throws E {
            TX transaction = manager.currentTransaction();
            if (transaction != null) {
                throw new IllegalStateException("No transaction expected.");
            }

            return callback.apply(null);
        }
    },
    /**
     * If a transaction exists, a new, nested transaction is created. Otherwise, a new root transaction is created.
     */
    NESTED {
        @Override
        public <R, E extends Throwable, TX extends Transaction> R applyTo(
                TransactionProperties properties,
                TransactionFactory<TX> factory,
                TransactionManager<TX> manager,
                ThrowingFunction<TX, R, E> callback
        ) throws E {
            TX transaction = manager.currentTransaction();
            if (transaction == null) {
                transaction = factory.createNewTransaction(properties);
            } else {
                transaction = factory.createNestedTransaction(transaction, properties);
            }

            return callback.apply(transaction);

        }
    };

    public abstract <R, E extends Throwable, TX extends Transaction> R applyTo(
            TransactionProperties properties,
            TransactionFactory<TX> factory,
            TransactionManager<TX> manager,
            ThrowingFunction<TX, R, E> callback
    ) throws E;
}
