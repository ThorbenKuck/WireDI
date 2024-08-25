package com.wiredi.runtime.domain.errors.results;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SequenceErrorHandlingResult<T extends Throwable> implements ExceptionHandlingResult<T> {

    private final List<@NotNull ExceptionHandlingResult<T>> results = new ArrayList<>();

    @NotNull
    public SequenceErrorHandlingResult<T> append(@NotNull ExceptionHandlingResult<T> error) {
        this.results.add(error);
        return this;
    }

    @Override
    @NotNull
    public ExceptionHandlingResult<T> then(@NotNull ExceptionHandlingResult<T> error) {
        return append(error);
    }

    @Override
    public boolean apply() throws Throwable {
        for (ExceptionHandlingResult<T> result : results) {
            if (!result.apply()) {
                return false;
            }
        }

        return true;
    }
}
