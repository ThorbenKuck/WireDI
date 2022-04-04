package com.github.thorbenkuck.di.properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PropertyConverter<T> {

    @Nullable
    T apply(
            @NotNull final TypedProperties typedProperties,
            @NotNull final String key,
            @Nullable final String defaultValue
    );

}
