package com.wiredi.runtime.properties;

import com.wiredi.runtime.collections.TypeMap;
import com.wiredi.runtime.properties.converter.*;
import com.wiredi.runtime.properties.exceptions.InvalidPropertyTypeException;
import com.wiredi.runtime.properties.exceptions.MissingTypeConverterException;
import com.wiredi.runtime.properties.exceptions.TypeConversionFailedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * A class to convert strings to other classes, especially used for property conversion.
 */
public final class TypeMapper {

    private final TypeMap<@NotNull PropertyConverter<?>> typeMappings;
    private static final TypeMapper INSTANCE = new TypeMapper();

    static {
        // Primitive types
        INSTANCE.setTypeConverter(boolean.class, BooleanPropertyConverter.INSTANCE);
        INSTANCE.setTypeConverter(int.class, IntPropertyConverter.INSTANCE);
        INSTANCE.setTypeConverter(float.class, FloatPropertyConverter.INSTANCE);
        INSTANCE.setTypeConverter(double.class, DoublePropertyConverter.INSTANCE);

        // Boxed Primitive Types
        INSTANCE.setTypeConverter(Boolean.class, BooleanPropertyConverter.INSTANCE);
        INSTANCE.setTypeConverter(Integer.class, IntPropertyConverter.INSTANCE);
        INSTANCE.setTypeConverter(Float.class, FloatPropertyConverter.INSTANCE);
        INSTANCE.setTypeConverter(Double.class, DoublePropertyConverter.INSTANCE);

        // Other default types
        INSTANCE.setTypeConverter(String.class, StringPropertyConverter.INSTANCE);
        INSTANCE.setTypeConverter(CharSequence.class, CharSequencePropertyConverter.INSTANCE);
        INSTANCE.setTypeConverter(Duration.class, DurationPropertyConverter.INSTANCE);
    }

    private TypeMapper() {
        this.typeMappings = new TypeMap<>();
    }

    private TypeMapper(TypeMap<@NotNull PropertyConverter<?>> typeMappings) {
        this.typeMappings = new TypeMap<>(typeMappings);
    }

    public static TypeMapper newPreconfigured() {
        return new TypeMapper(INSTANCE.typeMappings);
    }

    public static TypeMapper newEmpty() {
        return new TypeMapper();
    }

    public static TypeMapper getInstance() {
        return INSTANCE;
    }

    public void setTypeConverters(TypeMapper typeMapper) {
        this.typeMappings.clear();
        this.typeMappings.putAll(typeMapper.typeMappings);
    }

    /**
     * Registers a type converter to be used for the specified type.
     *
     * @param type      the type to convert property values to
     * @param converter the converter
     * @param <T>       the generic of the converter result
     * @return          the previous PropertyConverter associated with the type, or null if there was no converter for type.
     */
    @Nullable
    public <T> PropertyConverter<?> setTypeConverter(@NotNull final Class<T> type, @NotNull final PropertyConverter<T> converter) {
        return typeMappings.put(type, converter);
    }

    /**
     * Converts the provided value to the requested type.
     * <p>
     * This method accepts a {@code key}, for which the type conversion was requested.
     * It behaves the same was as {@link #parse(Class, String)}, but throws a more specialized exception
     * if the {@link PropertyConverter} fails to convert the property.
     * This {@link TypeConversionFailedException} references the {@code key} that was requested.
     *
     * @param type  The type to convert to
     * @param key   The key of the property
     * @param value The property value
     * @param <T>   The type to convert to
     * @return the converted type
     * @throws MissingTypeConverterException if no converter is registered for the type
     * @throws TypeConversionFailedException if the PropertyConverter failed to convert
     * @see #parse(Class, String)
     * @see #tryParse(Class, String)
     */
    public <T> @NotNull T parse(
            @NotNull final Class<T> type,
            @NotNull final Key key,
            @NotNull final String value
    ) {
        T result = tryParse(type, value);
        if (result == null) {
            throw new TypeConversionFailedException(type, key, value);
        }
        return result;
    }

    /**
     * Converts the provided value to the requested type.
     *
     * @param type  The type to convert to
     * @param value The property value
     * @param <T>   The type to convert to
     * @return the converted type
     * @throws MissingTypeConverterException if no converter is registered for the type
     * @throws TypeConversionFailedException if the PropertyConverter failed to convert
     * @see #parse(Class, Key, String)
     * @see #tryParse(Class, String)
     */
    public <T> @NotNull T parse(
            @NotNull final Class<T> type,
            @NotNull final String value
    ) {
        T result = tryParse(type, value);
        if (result == null) {
            throw new InvalidPropertyTypeException(value, type);
        }
        return result;
    }

    /**
     * Tries to convert the provided {@code value} to the requested type, returning null if it could not be converted.
     *
     * @param type  The type to convert to
     * @param value The property value
     * @param <T>   The type to convert to
     * @return the converted type
     * @throws MissingTypeConverterException if no converter is registered for the type
     * @throws TypeConversionFailedException if the PropertyConverter failed to convert
     * @see #parse(Class, Key, String)
     * @see #parse(Class, String)
     */
    public <T> @Nullable T tryParse(
            @NotNull final Class<T> type,
            @NotNull final String value
    ) {
        PropertyConverter<T> converter = (PropertyConverter<T>) typeMappings.get(type);

        if (converter == null && type.isEnum()) {
            converter = (PropertyConverter<T>) EnumPropertyConverter.INSTANCE;
        }
        if (converter == null) {
            throw new MissingTypeConverterException(type, value);
        }

        return converter.parse(type, value);
    }

    /**
     * Converts a property into a String representation.
     * <p>
     * If no {@link PropertyConverter} could be found for the type of the {@code value},
     * it is resolved using {@code value.toString()}
     *
     * @param value the value to stringify
     * @return a String representation
     */
    public @NotNull String stringify(Object value) {
        Class<?> type = value.getClass();
        PropertyConverter<?> converter = typeMappings.get(type);

        if (converter == null && type.isEnum()) {
            converter = EnumPropertyConverter.INSTANCE;
        }
        if (converter == null) {
            return value.toString();
        }

        return ((PropertyConverter<Object>) converter).stringify(value);
    }
}
