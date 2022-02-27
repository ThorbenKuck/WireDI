package com.github.thorbenkuck.di.properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PropertiesNotFoundException extends RuntimeException {
    public PropertiesNotFoundException(
            @NotNull final String fileName,
            @Nullable final Throwable throwable
    ) {
        super("Could not find the property: \"" + fileName + "\" in classpath.", throwable);
    }
}
