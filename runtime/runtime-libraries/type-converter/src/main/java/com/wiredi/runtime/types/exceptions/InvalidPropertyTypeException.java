package com.wiredi.runtime.types.exceptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Thrown when a conversion to a requested target type cannot be performed.
 *
 * The {@link com.wiredi.runtime.types.TypeMapper} raises this exception if none of the registered
 * converters can produce a value for the given input and target class. Callers typically see this
 * when requesting an unsupported conversion such as mapping an arbitrary object to a primitive type
 * without a suitable converter.
 *
 * Review your configured {@code TypeConverter} implementations or adjust the requested target type
 * if you encounter this exception during property or configuration reads.
 */
public class InvalidPropertyTypeException extends RuntimeException {

    public InvalidPropertyTypeException(
            @NotNull final Object value,
            @NotNull final Class<?> type
    ) {
        this(value, type, null);
    }

    public InvalidPropertyTypeException(
            @NotNull final Object value,
            @NotNull final Class<?> type,
            @Nullable final Throwable cause
    ) {
        super("The property " + value + " could not be converted to " + type, cause);
    }

}
