package com.wiredi.runtime.types.exceptions;

import java.util.List;

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
