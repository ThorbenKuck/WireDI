package com.wiredi.runtime.transactions;

import java.sql.Connection;

/**
 * Isolation levels for transactional work. The constants mirror the standard JDBC isolation levels from
 * {@link java.sql.Connection} so that backends can map directly when applicable.
 * <p>
 * The effective isolation level depends on the underlying resource. For example, a JPA implementation backed by
 * JDBC may set the level on the {@link Connection} or ignore it if the database or driver does not support changes
 * at runtime. Consumers should treat this as a hint.
 */
public enum TransactionIsolation {
    NONE(Connection.TRANSACTION_NONE),
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    private final int value;

    TransactionIsolation(int value) {
        this.value = value;
    }

    /**
     * The JDBC constant used to represent this isolation level.
     */
    public int value() {
        return value;
    }
}
