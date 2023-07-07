package com.wiredi.properties.converter;

import org.jetbrains.annotations.NotNull;

public interface PropertyConverter<T> {

    @NotNull
    T apply(
            @NotNull final String key,
            @NotNull final String value
    );

    static PropertyConverter<String> identity() {
        return (key, value) -> value;
    }
}
