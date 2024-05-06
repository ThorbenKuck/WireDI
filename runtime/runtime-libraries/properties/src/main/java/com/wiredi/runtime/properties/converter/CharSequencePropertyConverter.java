package com.wiredi.runtime.properties.converter;

import com.wiredi.runtime.properties.PropertyConverter;
import org.jetbrains.annotations.NotNull;

public class CharSequencePropertyConverter implements PropertyConverter<CharSequence> {

    public static final CharSequencePropertyConverter INSTANCE = new CharSequencePropertyConverter();

    @Override
    public @NotNull CharSequence parse(
            @NotNull Class<CharSequence> type,
            @NotNull String propertyValue
    ) {
        return propertyValue;
    }

    @Override
    public @NotNull String stringify(@NotNull CharSequence propertyValue) {
        if (propertyValue instanceof String) {
            return (String) propertyValue;
        }
        return String.valueOf(propertyValue);
    }
}
