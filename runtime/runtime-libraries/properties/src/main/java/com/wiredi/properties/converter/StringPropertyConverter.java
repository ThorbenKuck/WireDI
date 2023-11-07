package com.wiredi.properties.converter;

import org.jetbrains.annotations.NotNull;

public class StringPropertyConverter implements PropertyConverter<String> {

    public static final StringPropertyConverter INSTANCE = new StringPropertyConverter();

    @Override
    public @NotNull String apply(@NotNull String key, @NotNull String value) {
        return value;
    }
}