package com.wiredi.runtime.domain.errors.results;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SequenceErrorHandlingResult<T extends Throwable> implements ErrorHandlingResult<T> {

    private final List<@NotNull ErrorHandlingResult<T>> results = new ArrayList<>();

    @NotNull
    public SequenceErrorHandlingResult<T> append(@NotNull ErrorHandlingResult<T> error) {
        this.results.add(error);
        return this;
    }

    @Override
    @NotNull
    public ErrorHandlingResult<T> then(@NotNull ErrorHandlingResult<T> error) {
        return append(error);
    }

    @Override
    public boolean apply() throws Throwable {
        for (ErrorHandlingResult<T> result : results) {
            if (!result.apply()) {
                return false;
            }
        }

        return true;
    }
}
