package com.wiredi.runtime.transactions.exception;

public class TransactionRollbackException extends RuntimeException {
    public TransactionRollbackException(Throwable cause) {
        super(cause);
    }

    public TransactionRollbackException() {}
}
