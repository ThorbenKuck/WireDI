package com.wiredi.retry.exception;

import org.jetbrains.annotations.NotNull;

public final class UnsupportedRetryException extends RetryException {
    public UnsupportedRetryException(@NotNull final Throwable throwable) {
        super(throwable);
    }
}
