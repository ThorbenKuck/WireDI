package com.github.thorbenkuck.di.properties;

public class InvalidPropertySyntaxException extends RuntimeException {

    public InvalidPropertySyntaxException(String s, Throwable throwable) {
        super("Invalid property file content: " + s, throwable);
    }
}
