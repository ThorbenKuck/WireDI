package com.wiredi.runtime.domain.errors.results;

public class RethrowingErrorHandlingResult<T extends Throwable> implements ErrorHandlingResult<T> {

    private final Throwable rethrow;

    public RethrowingErrorHandlingResult(Throwable rethrow) {
        this.rethrow = rethrow;
    }

    @Override
    public boolean apply() throws Throwable {
        throw rethrow;
    }
}
