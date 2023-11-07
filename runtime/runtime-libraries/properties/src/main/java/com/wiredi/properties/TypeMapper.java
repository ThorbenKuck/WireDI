package com.wiredi.properties;

import com.wiredi.lang.collections.TypeMap;
import com.wiredi.properties.converter.*;
import com.wiredi.properties.exceptions.MissingTypeConverterException;
import com.wiredi.properties.keys.Key;
import org.jetbrains.annotations.NotNull;

/**
 * A static utility class to map certain types in the typed properties.
 */
public final class TypeMapper {

    @NotNull
    private static final TypeMap<@NotNull PropertyConverter<?>> typeMappings = new TypeMap<>();

    static {
        setTypeConverter(boolean.class, BooleanPropertyConverter.INSTANCE);
        setTypeConverter(int.class, IntPropertyConverter.INSTANCE);
        setTypeConverter(float.class, FloatPropertyConverter.INSTANCE);
        setTypeConverter(double.class, DoublePropertyConverter.INSTANCE);

        setTypeConverter(Boolean.class, BooleanPropertyConverter.INSTANCE);
        setTypeConverter(Integer.class, IntPropertyConverter.INSTANCE);
        setTypeConverter(Float.class, FloatPropertyConverter.INSTANCE);
        setTypeConverter(Double.class, DoublePropertyConverter.INSTANCE);

        setTypeConverter(String.class, StringPropertyConverter.INSTANCE);
    }

    /**
     * Registers a type converter to be used for the specified type.
     *
     * @param type      the type to convert property values to
     * @param converter the converter
     * @param <T>       the generic of the converter result
     */
    public static <T> void setTypeConverter(@NotNull final Class<T> type, @NotNull final PropertyConverter<T> converter) {
        typeMappings.put(type, converter);
    }

    /**
     * Converts the provided value to the provided type
     *
     * @param type The type to convert to
     * @param key The key of the property
     * @param value The property value
     * @return the converted type
     * @param <T> The type to convert to
     * @throws MissingTypeConverterException if no converter is registered for the type
     */
    public static <T> @NotNull T convert(@NotNull final Class<T> type, @NotNull final Key key, @NotNull final String value) {
        final PropertyConverter<?> conversion = typeMappings.get(type);
        if (conversion == null) {
            throw new MissingTypeConverterException(type, value);
        }
        return (T) conversion.apply(key.value(), value);
    }
}
