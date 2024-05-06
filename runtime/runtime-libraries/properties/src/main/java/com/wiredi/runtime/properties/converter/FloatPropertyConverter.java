package com.wiredi.runtime.properties.converter;

import com.wiredi.runtime.properties.PropertyConverter;
import com.wiredi.runtime.properties.exceptions.InvalidPropertyTypeException;
import org.jetbrains.annotations.NotNull;

public class FloatPropertyConverter implements PropertyConverter<Float> {

    public static final FloatPropertyConverter INSTANCE = new FloatPropertyConverter();

    @Override
    public @NotNull Float parse(
            @NotNull final Class<Float> type,
            @NotNull String propertyValue
    ) {
        try {
            return Float.parseFloat(propertyValue);
        } catch (final NumberFormatException numberFormatException) {
            throw new InvalidPropertyTypeException(propertyValue, Integer.class);
        }
    }

    @Override
    public @NotNull String stringify(@NotNull Float propertyValue) {
        return Float.toString(propertyValue);
    }
}
