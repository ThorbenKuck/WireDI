package com.wiredi.properties.exceptions;

import org.jetbrains.annotations.NotNull;

public class InvalidPropertySyntaxException extends RuntimeException {

    public InvalidPropertySyntaxException(
            @NotNull final String s,
            @NotNull final Throwable throwable
    ) {
        super("Invalid property file content: " + s, throwable);
    }
}
