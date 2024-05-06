package com.wiredi.runtime.properties.converter;

import com.wiredi.runtime.properties.PropertyConverter;
import com.wiredi.runtime.properties.exceptions.TypeConversionFailedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class DurationPropertyConverter implements PropertyConverter<Duration> {

    public static final DurationPropertyConverter INSTANCE = new DurationPropertyConverter();

    @Override
    public @Nullable Duration parse(@NotNull Class<Duration> type, @NotNull String propertyValue) throws TypeConversionFailedException {
        return Duration.parse(propertyValue);
    }

    @Override
    public @NotNull String stringify(@NotNull Duration propertyValue) {
        return propertyValue.toString();
    }
}
