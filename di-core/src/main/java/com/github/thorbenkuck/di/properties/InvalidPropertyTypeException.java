package com.github.thorbenkuck.di.properties;

public class InvalidPropertyTypeException extends RuntimeException {

    public InvalidPropertyTypeException(String key, String value, Class<?> type) {
        this(key, value, type, null);
    }

    public InvalidPropertyTypeException(String key, String value, Class<?> type, Throwable cause) {
        super("The property " + key + " with value " + value + " could not be parsed as " + type.getSimpleName(), cause);
    }

}
