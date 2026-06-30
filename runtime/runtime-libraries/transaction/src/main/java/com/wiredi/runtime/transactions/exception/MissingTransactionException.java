package com.wiredi.runtime.transactions.exception;

public class MissingTransactionException extends RuntimeException {
    public MissingTransactionException() {
    }

    public MissingTransactionException(Throwable cause) {
        super(cause);
    }

    public MissingTransactionException(String message) {
        super(message);
    }

    public MissingTransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingTransactionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
