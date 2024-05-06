package com.wiredi.runtime.properties.converter;

import com.wiredi.runtime.collections.EnumSet;
import com.wiredi.runtime.properties.PropertyConverter;
import org.jetbrains.annotations.NotNull;

public class EnumPropertyConverter<T extends Enum<T>> implements PropertyConverter<T> {

    public static final EnumPropertyConverter INSTANCE = new EnumPropertyConverter();

    @Override
    public @NotNull T parse(
            @NotNull Class<T> type,
            @NotNull String propertyValue
    ) {
        return EnumSet.of(type).require(propertyValue.toUpperCase().replaceAll("-", "_"));
    }

    @Override
    public @NotNull String stringify(@NotNull T propertyValue) {
        return propertyValue.name();
    }
}
