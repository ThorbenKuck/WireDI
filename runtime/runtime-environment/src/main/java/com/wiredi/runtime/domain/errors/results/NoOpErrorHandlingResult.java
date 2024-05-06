package com.wiredi.runtime.domain.errors.results;

public class NoOpErrorHandlingResult<T extends Throwable> implements ErrorHandlingResult<T> {

    public static final NoOpErrorHandlingResult<Throwable> INSTANCE = new NoOpErrorHandlingResult<>();

    @Override
    public boolean apply() throws Throwable {
        return true;
    }
}
