package com.wiredi.properties.exceptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PropertiesNotFoundException extends RuntimeException {
    public PropertiesNotFoundException(
            @NotNull final String fileName,
            @Nullable final Throwable throwable
    ) {
        super("Could not find the property: \"" + fileName + "\" in classpath.", throwable);
    }
    public PropertiesNotFoundException(
            @Nullable final Throwable throwable
    ) {
        super("Property could not be loaded.", throwable);
    }
}
