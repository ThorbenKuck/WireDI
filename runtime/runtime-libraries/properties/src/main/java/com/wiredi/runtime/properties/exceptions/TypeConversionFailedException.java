package com.wiredi.runtime.properties.exceptions;

import com.wiredi.runtime.properties.Key;

public class TypeConversionFailedException extends RuntimeException {

    public TypeConversionFailedException(Class<?> type, Key key, String value) {
        super("Failed to convert the Property(key=" + key + ", value=" + value + ") to type " + type);
    }
}
