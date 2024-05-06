package com.wiredi.runtime.retry.exception;

import org.jetbrains.annotations.NotNull;

public class RetryException extends RuntimeException {

    public RetryException() {
    }

    public RetryException(@NotNull final String message) {
        super(message);
    }

    public RetryException(@NotNull final Throwable cause) {
        super(cause);
    }

    public RetryException(@NotNull final String message, @NotNull final Throwable cause) {
        super(message, cause);
    }
}
