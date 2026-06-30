package com.wiredi.runtime.transactions;

import com.wiredi.runtime.lang.ThrowingConsumer;
import com.wiredi.runtime.lang.ThrowingFunction;
import com.wiredi.runtime.lang.ThrowingRunnable;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * High-level API to execute code within transactional boundaries.
 * <p>
 * An implementation such as {@link TransactionStateManager} manages a per-thread current transaction and ensures
 * that transactions are flushed after execution. This interface offers overloads for both void and returning lambdas.
 * <p>
 * Typical usage in application code:
 * <pre>{@code
 * TransactionManager tm = ...;
 * tm.run(status -> repository.save(entity));
 * String data = tm.call(status -> service.load());
 * }
 * </pre>
 * <p>
 * Integrating a persistence module (e.g. JPA): provide a {@link TransactionFactory} that creates a {@link Transaction}
 * bound to your EntityManager/Session and construct a {@link TransactionStateManager} with it.
 */
public interface TransactionManager<TX extends Transaction> {

    /**
     * Create a new root transaction.
     * <p>
     * After calling this method, the TransactionManager must bound the transaction to the current thread, nesting
     * any existing transaction.
     * Meaning, the following code must be valid:
     *
     * <pre>{@code
     * TransactionManager tm = ...;
     * TransactionProperties properties = ...;
     * Transaction t1 = tm.createNewTransaction(properties);
     * Transaction t2 = tm.currentTransaction();
     *
     * assert t1 == t2;
     * }</pre>
     */
    @NotNull
    TX createNewTransaction(@NotNull TransactionProperties properties);

    /**
     * Create a new root transaction.
     * <p>
     * After calling this method, the TransactionManager must bound the transaction to the current thread, nesting
     * any existing transaction.
     * Meaning, the following code must be valid:
     *
     * <pre>{@code
     * TransactionManager tm = ...;
     * TransactionProperties properties = ...;
     * Transaction t1 = tm.createNewTransaction(properties);
     * Transaction t2 = tm.currentTransaction();
     *
     * assert t1 == t2;
     * }</pre>
     */
    @NotNull
    TX createNewNestedTransaction(@NotNull TransactionProperties properties);

    /**
     * Get the transaction currently bound to this thread, or {@code null} if none.
     */
    @Nullable
    TX currentTransaction();

    @NotNull TransactionStateManager.Suspended suspend();

    void resume();

    default <T, E extends Exception> @NotNull T get(@NotNull ThrowingSupplier<@NotNull T, E> supplier) throws E {
        return get(TransactionProperties.DEFAULT, tx -> supplier.get());
    }

    default <T, E extends Exception> @NotNull T get(@NotNull ThrowingFunction<TX, @NotNull T, E> function) throws E {
        return get(TransactionProperties.DEFAULT, function);
    }

    <T, E extends Exception> @NotNull T get(@NotNull TransactionProperties properties, @NotNull ThrowingFunction<TX, @NotNull T, E> function) throws E;

    default <T, E extends Exception> @NotNull Optional<T> tryGet(@NotNull ThrowingSupplier<@Nullable T, E> supplier) throws E {
        return tryGet(TransactionProperties.DEFAULT, tx -> supplier.get());
    }

    default <T, E extends Exception> @NotNull Optional<T> tryGet(@NotNull ThrowingFunction<TX, @Nullable T, E> function) throws E {
        return tryGet(TransactionProperties.DEFAULT, function);
    }

    <T, E extends Exception> @NotNull Optional<T> tryGet(@NotNull TransactionProperties properties, @NotNull ThrowingFunction<TX, @Nullable T, E> function) throws E;

    default <E extends Exception> void run(@NotNull ThrowingRunnable<E> function) throws E {
        run(TransactionProperties.DEFAULT, tx -> function.run());
    }

    default <E extends Exception> void run(@NotNull ThrowingConsumer<TX, E> function) throws E {
        run(TransactionProperties.DEFAULT, function);
    }

    <E extends Exception> void run(@NotNull TransactionProperties properties, @NotNull ThrowingConsumer<TX, E> function) throws E;
}
