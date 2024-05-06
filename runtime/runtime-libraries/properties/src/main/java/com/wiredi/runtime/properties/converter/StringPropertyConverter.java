package com.wiredi.runtime.properties.converter;

import com.wiredi.runtime.properties.PropertyConverter;
import org.jetbrains.annotations.NotNull;

public class StringPropertyConverter implements PropertyConverter<String> {

    public static final StringPropertyConverter INSTANCE = new StringPropertyConverter();

    @Override
    public @NotNull String parse(
            @NotNull final Class<String> type,
            @NotNull String propertyValue
    ) {
        return propertyValue;
    }

    @Override
    public @NotNull String stringify(@NotNull String propertyValue) {
        return propertyValue;
    }
}