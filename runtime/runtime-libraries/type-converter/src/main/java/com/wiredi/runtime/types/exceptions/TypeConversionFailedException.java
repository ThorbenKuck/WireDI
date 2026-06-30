package com.wiredi.runtime.types.exceptions;

import java.util.List;

/**
 * Indicates that a {@link com.wiredi.runtime.types.TypeConverter} attempted a conversion and failed.
 *
 * This exception carries context about the target type and the source value that could not be
 * converted. Converters throw this to signal a hard failure for an otherwise supported source type,
 * for example due to malformed input. The {@link com.wiredi.runtime.types.TypeMapper} may wrap and
 * propagate this error to callers of conversion APIs.
 */
public class TypeConversionFailedException extends RuntimeException {

    public TypeConversionFailedException(Class<?> type, String value) {
        super("Failed to convert the Property(" + value + ") to type " + type);
    }

    public TypeConversionFailedException(Class<?> type, String value, Throwable cause) {
        super("Failed to convert the Property(" + value + ") to type " + type, cause);
    }

    public <T> TypeConversionFailedException(List<Class<T>> type, String value, Throwable cause) {
        super("Failed to convert the Property(" + value + ") to either type of " + type, cause);
    }
}
