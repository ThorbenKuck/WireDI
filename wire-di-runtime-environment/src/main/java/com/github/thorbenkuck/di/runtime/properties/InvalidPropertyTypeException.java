package com.github.thorbenkuck.di.runtime.properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InvalidPropertyTypeException extends RuntimeException {

    public InvalidPropertyTypeException(
            @NotNull final String key,
            @NotNull final String value,
            @NotNull final Class<?> type
    ) {
        this(key, value, type, null);
    }

    public InvalidPropertyTypeException(
            @NotNull final String key,
            @NotNull final String value,
            @NotNull final Class<?> type,
            @Nullable final Throwable cause
    ) {
        super("The property " + key + " with value " + value + " could not be parsed as " + type.getSimpleName(), cause);
    }

}
