package com.wiredi.runtime.transactions;

/**
 * Immutable configuration for creating transactions.
 * <p>
 * Properties include read-only hint, propagation behavior and isolation level. The actual semantics of these
 * properties depend on the underlying {@link TransactionFactory} and {@link Transaction} implementation
 * (e.g. a JPA-backed implementation may map isolation to JDBC when possible).
 * <p>
 * Use {@link #DEFAULT} for common cases or the {@link Builder} to override individual aspects.
 */
public record TransactionProperties(
        boolean readOnly,
        TransactionPropagation propagation,
        TransactionIsolation isolation
) {

    /**
     * Default properties: read-write, propagation SUPPORTED, isolation NONE.
     */
    public static final TransactionProperties DEFAULT = new TransactionProperties(false, TransactionPropagation.NESTED, TransactionIsolation.NONE);

    /**
     * Start building from the {@link #DEFAULT} configuration.
     */
    public static Builder builder() {
        return new Builder(DEFAULT);
    }

    /**
     * Start building from an arbitrary base configuration.
     */
    public static Builder builder(TransactionProperties base) {
        return new Builder(base);
    }

    /**
     * Fluent builder for {@link TransactionProperties}.
     */
    public static class Builder {
        private boolean readOnly;
        private TransactionPropagation propagation;
        private TransactionIsolation isolation;

        public Builder(TransactionProperties base) {
            this.readOnly = base.readOnly;
            this.propagation = base.propagation;
            this.isolation = base.isolation;
        }

        /**
         * Read-only hint for backends that can optimize read transactions.
         */
        public Builder readOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        /**
         * Define the propagation behavior when a transaction is already active.
         */
        public Builder propagation(TransactionPropagation propagation) {
            this.propagation = propagation;
            return this;
        }

        /**
         * Choose the isolation level; effective support depends on the backend.
         */
        public Builder isolation(TransactionIsolation isolation) {
            this.isolation = isolation;
            return this;
        }

        /**
         * Create the immutable {@link TransactionProperties} instance.
         */
        public TransactionProperties build() {
            return new TransactionProperties(readOnly, propagation, isolation);
        }
    }
}
