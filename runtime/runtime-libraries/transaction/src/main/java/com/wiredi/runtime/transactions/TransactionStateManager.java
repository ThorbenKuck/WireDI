package com.wiredi.runtime.transactions;

import com.wiredi.runtime.lang.ThrowingConsumer;
import com.wiredi.runtime.lang.ThrowingFunction;
import com.wiredi.runtime.transactions.exception.InactiveTransactionException;
import com.wiredi.runtime.transactions.exception.MissingTransactionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Default, thread-local TransactionManager implementation.
 * <p>
 * This manager delegates creation of transaction instances to a pluggable {@link TransactionFactory}, maintains
 * a per-thread pointer to the current transaction, and guarantees that a transaction is flushed after user code
 * completes. Exceptions thrown by user code are forwarded to {@link Transaction#handleThrowable(Throwable)} which
 * typically marks the transaction as rollback-only.
 * <p>
 * Example usage:
 * <pre>{@code
 * TransactionManager tm = new TransactionStateManager(new JpaTransactionFactory(entityManager));
 * String result = tm.call(status -> {
 *   // business logic
 *   return service.compute();
 * });
 * }</pre>
 * <p>
 * Integrating with JPA (Hibernate, EclipseLink, etc.):
 * <pre>{@code
 * public final class JpaTransactionFactory implements TransactionFactory {
 *   private final EntityManager em;
 *   public JpaTransactionFactory(EntityManager em) { this.em = em; }
 *
 *   @Override
 *   public Transaction createNewTransaction(@NotNull TransactionProperties properties) {
 *     return new JpaTransaction(null, em);
 *   }
 *
 *   @Override
 *   public Transaction createNestedTransaction(@NotNull Transaction parent, @NotNull TransactionProperties properties) {
 *     // For JPA, nested transactions without savepoints are not flushable; delegate callbacks to parent
 *     return new JpaTransaction(parent, em);
 *   }
 * }
 * }</pre>
 */
public class TransactionStateManager<TX extends Transaction> implements TransactionManager<TX> {

    private static final ThreadLocal<Transaction> transactionPointer = new ThreadLocal<>();
    private static final ThreadLocal<Deque<Transaction>> suspendedStack = ThreadLocal.withInitial(ArrayDeque::new);
    private final TransactionFactory<TX> transactionFactory;

    public TransactionStateManager(TransactionFactory<TX> transactionFactory) {
        this.transactionFactory = transactionFactory;
    }

    public static boolean currentTransactionExists() {
        return transactionPointer.get() != null;
    }

    @Nullable
    public static <TX extends Transaction> TX getTransaction() {
        return (TX) transactionPointer.get();
    }

    public static void registerTransactionCallback(TransactionCallback callback) {
        Transaction transaction = transactionPointer.get();
        if (transaction == null) {
            throw new MissingTransactionException("Cannot register TransactionCallback without active transaction");
        }
        transaction.registerCallback(callback);
    }

    @Override
    public @NotNull TX createNewTransaction(@NotNull TransactionProperties properties) {
        TX newTransaction = transactionFactory.createNewTransaction(properties);
        transactionPointer.set(newTransaction);
        return newTransaction;
    }

    @Override
    public @NotNull TX createNewNestedTransaction(@NotNull TransactionProperties properties) {
        TX transaction = currentTransaction();
        if (transaction == null) {
            throw new InactiveTransactionException("Cannot create nested transaction: no active transaction associated with the current thread.");
        }
        TX newTransaction = transactionFactory.createNestedTransaction(transaction, properties);
        transactionPointer.set(newTransaction);
        return newTransaction;
    }

    @Override
    public TX currentTransaction() {
        return getTransaction();
    }

    /**
     * Suspends the current transaction for this thread, if present, and returns a handle
     * that can be used to resume it later. While suspended, currentTransactionExists() will
     * return false and getTransaction() will return null.
     * <p>
     * Suspension is stack-based: multiple suspensions must be resumed in LIFO order.
     */
    @Override
    public @NotNull Suspended suspend() {
        Transaction current = transactionPointer.get();
        if (current == null) {
            throw new MissingTransactionException("Cannot suspend: no active transaction associated with the current thread.");
        }

        // Optional hook: if your TX supports resource unbinding, call it here.
        // For now, we keep it purely logical (pointer only).
        Deque<Transaction> stack = suspendedStack.get();
        stack.push(current);
        transactionPointer.remove();

        return new Suspended(this);
    }

    /**
     * Resumes the most recently suspended transaction for this thread.
     * If there is no suspended transaction, throws MissingTransactionException.
     */
    @Override
    public void resume() {
        Deque<Transaction> stack = suspendedStack.get();
        Transaction toResume = (stack.isEmpty() ? null : stack.pop());
        if (toResume == null) {
            throw new MissingTransactionException("Cannot resume: no suspended transaction for this thread.");
        }
        transactionPointer.set(toResume);

        // Optional hook: if your TX supports resource re-binding, call it here.
    }

    private void resetPointer(Transaction previous) {
        if (previous != null) {
            transactionPointer.set(previous);
        } else {
            transactionPointer.remove();
        }
    }

    /**
     * Execute the given function within a transaction and return its result.
     * <p>
     * Exceptions trigger {@link Transaction#handleThrowable(Throwable)} and are then rethrown to the caller.
     * The transaction is flushed and the pointer restored in a finally block.
     */
    @Override
    public <T, E extends Exception> @NotNull Optional<T> tryGet(@NotNull TransactionProperties properties, @NotNull ThrowingFunction<@NotNull TX, @Nullable T, E> function) throws E {
        TX previousTransaction = currentTransaction();
        return properties.propagation().applyTo(properties, transactionFactory, this, transaction -> {
            transactionPointer.set(transaction);
            try {
                return Optional.ofNullable(function.apply(transaction));
            } catch (Throwable e) {
                transaction.handleThrowable(e);
                throw e;
            } finally {
                transactionPointer.set(previousTransaction);
                transaction.flush();
            }
        });
    }

    /**
     * Execute the given function within a transaction and return its result.
     * <p>
     * Exceptions trigger {@link Transaction#handleThrowable(Throwable)} and are then rethrown to the caller.
     * The transaction is flushed and the pointer restored in a finally block.
     */
    @Override
    public <T, E extends Exception> @NotNull T get(@NotNull TransactionProperties properties, @NotNull ThrowingFunction<@NotNull TX, @NotNull T, E> function) throws E {
        TX previousTransaction = currentTransaction();
        return properties.propagation().applyTo(properties, transactionFactory, this, transaction -> {
            transactionPointer.set(transaction);
            try {
                return function.apply(transaction);
            } catch (Throwable e) {
                transaction.handleThrowable(e);
                throw e;
            } finally {
                transactionPointer.set(previousTransaction);
                transaction.flush();
            }
        });
    }

    /**
     * Execute the given function within a transaction and return its result.
     * <p>
     * Exceptions trigger {@link Transaction#handleThrowable(Throwable)} and are then rethrown to the caller.
     * The transaction is flushed and the pointer restored in a finally block.
     */
    @Override
    public <E extends Exception> void run(@NotNull TransactionProperties properties, @NotNull ThrowingConsumer<@NotNull TX, E> function) throws E {
        TX previousTransaction = currentTransaction();
        properties.propagation().applyTo(properties, transactionFactory, this, transaction -> {
            transactionPointer.set(transaction);
            try {
                function.accept(transaction);
                return null;
            } catch (Throwable e) {
                transaction.handleThrowable(e);
                throw e;
            } finally {
                transactionPointer.set(previousTransaction);
                transaction.flush();
            }
        });
    }

    /**
     * A convenience AutoCloseable handle to use with try-with-resources:
     *
     * <pre>{@code
     * try (var s = tm.suspend()) {
     * // work without the original transaction
     * }
     * // auto-resume here
     * }</pre>
     */
    public static final class Suspended implements AutoCloseable {
        private final TransactionStateManager<?> owner;
        private boolean resumed = false;

        private Suspended(TransactionStateManager<?> owner) {
            this.owner = owner;
        }

        @Override
        public void close() {
            resume();
        }

        public void resume() {
            if (!resumed) {
                owner.resume();
                resumed = true;
            }
        }
    }
}
