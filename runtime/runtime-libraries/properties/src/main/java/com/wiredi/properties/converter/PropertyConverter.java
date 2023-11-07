package com.wiredi.properties.converter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PropertyConverter<T> {

    @Nullable
    T apply(
            @NotNull final String key,
            @NotNull final String value
    );
}
