package com.github.thorbenkuck.di.properties;

public class PropertiesNotFoundException extends RuntimeException {
    public PropertiesNotFoundException(String fileName, Throwable throwable) {
        super("Could not find the property: \"" + fileName + "\" in classpath.", throwable);
    }
}
