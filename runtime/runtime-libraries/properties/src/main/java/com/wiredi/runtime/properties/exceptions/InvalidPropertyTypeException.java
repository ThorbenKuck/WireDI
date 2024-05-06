package com.wiredi.runtime.properties.exceptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This exception is raised, if the provided {@code value} could not be converted to the {@code type}
 */
public class InvalidPropertyTypeException extends RuntimeException {

    public InvalidPropertyTypeException(
            @NotNull final String value,
            @NotNull final Class<?> type
    ) {
        this(value, type, null);
    }

    public InvalidPropertyTypeException(
            @NotNull final String value,
            @NotNull final Class<?> type,
            @Nullable final Throwable cause
    ) {
        super("The property " + value + " could not be converted to " + type, cause);
    }

}
