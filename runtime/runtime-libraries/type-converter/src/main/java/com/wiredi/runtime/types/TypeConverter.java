package com.wiredi.runtime.types;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Contract for mapping arbitrary input values into a specific target type.
 * <p>
 * A TypeConverter focuses on a single family of target types and knows how to construct that target
 * from one or more concrete source classes. The {@link #convert(Object)} method is invoked by the
 * {@link TypeMapper} during a conversion attempt. Implementations must either return a converted value
 * or return null to indicate that they cannot handle the given input. Returning null allows the mapper
 * to consult additional converters until one succeeds.
 * <p>
 * Converters should be stateless and thread-safe so that a single instance can be shared and cached.
 * Implement {@link #supports(Class)} and {@link #supportedSources()} to advertise which source classes
 * you can handle. For small, function-based converters that map a fixed set of source classes, extend
 * {@link TypeConverterBase} and register your sources in {@code setup()}.
 * <p>
 * Simple example: a converter that creates Strings from a handful of common sources would declare
 * String as its target type and register byte[], CharSequence and numeric primitives as supported
 * sources. When the mapper requests a String, this converter is queried with the concrete runtime class
 * of the input and returns a new String when supported.
 *
 * @param <T> the target type this converter produces
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
