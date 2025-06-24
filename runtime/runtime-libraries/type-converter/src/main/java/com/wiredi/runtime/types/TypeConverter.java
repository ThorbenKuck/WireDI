package com.wiredi.runtime.types;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An interface which defines how to convert from any input type to a specific target type.
 * <p>
 * For example, if you want to define a mapper, that describes how a String is mapped for other instances.
 * In this scenario, the target type would be a String,
 * whilst the input type (also called source) can be whatever instance.
 * <p>
 * Its {@link #convert(Object)} method is invoked, if any object should be converted into a type supported by
 * this type converter.
 * If it returns null, this converter was unable to convert it.
 * <p>
 * Optionally,
 * implementations can override {@link #supports(Class)} to signal if they can convert any specific implementation.
 * <p>
 * To make using this class easier, you can resort to the {@link TypeConverterBase}
 *
 * @param <T>
 * @see TypeConverterBase
 * @see TypeMapper
 */
public interface TypeConverter<T> {

    /**
     * Converts any input into the generic type of this TypeConverter.
     * <p>
     * If this method returns null, this mapper is not able to convert the input and allowing the {@link TypeMapper} to
     * consult additional converter instances.
     *
     * @param s   The input to convert into the type supported by this converter
     * @param <S> The generic type of the input.
     * @return A new Instance if mapping was successful, or null if this converter is unable to convert the input.
     */
    @Nullable
    <S> T convert(S s);

    /**
     * A list of all types that this converter can construct.
     *
     * @return all constructable types.
     */
    List<Class<T>> getTargetTypes();

    /**
     * A method that can be overridden optionally to signal if an input type can be mapped to the target type.
     * <p>
     * By default, this method returns true, meaning that any input will be attempted for conversion.
     * Override this method if you want to have more special logic.
     *
     * @param type The potential input type
     * @return true, if this converter can convert the input type
     */
    default boolean supports(Class<?> type) {
        return true;
    }

    /**
     * This method returns a list of types that this converter can support.
     * <p>
     * It is used when autoconfiguring TypeConverters through {@link TypeMapper#setTypeConverter(TypeConverter)},
     * which is used to automatically register a TypeConverter for all supported types.
     *
     * @return a list of all supported input types.
     */
    default Collection<Class<?>> supportedSources() {
        return Collections.emptyList();
    }
}
