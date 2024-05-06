package com.wiredi.runtime.properties;

import com.wiredi.runtime.properties.exceptions.TypeConversionFailedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PropertyConverter<T> {

    /**
     * Convert the provided property {@code propertyValue} to the generic type.
     * <p>
     * Implementations can return null if they could not convert the type but feel like other converters could,
     * or they can throw a {@link TypeConversionFailedException} if they feel like it is unrecoverable.
     *
     * @param type          the concrete class of the type that should be converted to
     * @param propertyValue the property value that should be converted
     * @return a converted instance, or null if this {@link PropertyConverter} could convert to the requested {@code type}
     * @throws TypeConversionFailedException if the converter failed to convert the propertyValue
     */
    @Nullable
    T parse(
            @NotNull final Class<T> type,
            @NotNull final String propertyValue
    ) throws TypeConversionFailedException;

    /**
     * Converts a given value to a string for generic storage.
     * <p>
     * Implementations must always return an instance if they are registered for the given type parameter {@code T}
     *
     * @param propertyValue the value to convert
     * @return a string representation of the provided {@code propertyValue}
     */
    @NotNull
    String stringify(@NotNull final T propertyValue);
}
