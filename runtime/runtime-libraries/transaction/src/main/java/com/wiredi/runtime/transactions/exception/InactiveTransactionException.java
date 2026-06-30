package com.wiredi.runtime.transactions.exception;

public class InactiveTransactionException extends RuntimeException {

    public InactiveTransactionException() {
    }

    public InactiveTransactionException(String message) {
        super(message);
    }

    public InactiveTransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InactiveTransactionException(Throwable cause) {
        super(cause);
    }

    public InactiveTransactionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
