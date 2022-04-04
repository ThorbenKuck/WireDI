package com.github.thorbenkuck.di.properties;

import org.jetbrains.annotations.NotNull;

public class PropertyNotFoundException extends RuntimeException {

    public PropertyNotFoundException(@NotNull final String property)  {
        super("Could not found the property \"" + property + "\"");
    }

    public PropertyNotFoundException(
            @NotNull final String property,
            @NotNull final String source
    )  {
        super("Could not found the property \"" + property + "\" in \"" + source + "\"");
    }
}
