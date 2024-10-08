package com.wiredi.runtime.domain.errors.results;

public class NotProcessedErrorHandlingResult<T extends Throwable> implements ExceptionHandlingResult<T> {

    public static final NotProcessedErrorHandlingResult<Throwable> INSTANCE = new NotProcessedErrorHandlingResult<>();

    @Override
    public boolean apply() throws Throwable {
        return false;
    }
}
