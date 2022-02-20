package com.github.thorbenkuck.di.properties;

public class PropertyNotFoundException extends RuntimeException {

    public PropertyNotFoundException(String property)  {
        super("Could not found the property \"" + property + "\"");
    }

    public PropertyNotFoundException(String property, String source)  {
        super("Could not found the property \"" + property + "\" in \"" + source + "\"");
    }
}
