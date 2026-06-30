package com.wiredi.runtime.transactions;

import org.jetbrains.annotations.NotNull;

/**
 * Factory SPI used by {@link TransactionStateManager} to create transaction instances.
 * <p>
 * A module integrates with WireDI transactions by providing an implementation of this interface and returning
 * {@link Transaction} objects that wrap the platform-specific transaction handle (e.g. JPA's EntityTransaction,
 * JDBC Connections, message broker sessions, etc.).
 * <p>
 * Example integration with JPA (simplified):
 * <pre>{@code
 * final class JpaTransactionFactory implements TransactionFactory {
 *   private final EntityManager em;
 *   JpaTransactionFactory(EntityManager em) { this.em = em; }
 *
 *   public Transaction createNewTransaction(@NotNull TransactionProperties properties) {
 *     return new JpaTransaction(null, em);
 *   }
 *
 *   public Transaction createNestedTransaction(@NotNull Transaction parent, @NotNull TransactionProperties properties) {
 *     // Without savepoints, nested transactions are not flushable and delegate callbacks to parent
 *     return new JpaTransaction(parent, em);
 *   }
 * }
 * }
 * </pre>
 */
public interface TransactionFactory<T extends Transaction> {

    /**
     * Create a new root transaction according to the provided properties.
     */
    T createNewTransaction(@NotNull TransactionProperties properties);

    /**
     * Create a child transaction for the given parent. Implementations may return non-flushable children that
     * delegate lifecycle to the parent unless savepoints are supported.
     */
    T createNestedTransaction(@NotNull Transaction parent, @NotNull TransactionProperties properties);

}
