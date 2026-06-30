package com.wiredi.runtime.types;

import com.wiredi.runtime.collections.TypeMap;
import com.wiredi.runtime.types.converter.*;
import com.wiredi.runtime.types.exceptions.InvalidPropertyTypeException;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Central entry point for simple, lossless type conversion at runtime.
 * <p>
 * The TypeMapper coordinates a set of {@link TypeConverter converters} to transform values from one Java type
 * into another. It is designed for primitive and near-primitive conversions such as String to Integer,
 * String to Duration, byte[] to String, or number widening/narrowing. For complex object graphs or full
 * serialization concerns you should use higher level facilities in the messaging/integration modules; TypeMapper
 * focuses on small, predictable transformations commonly needed while reading properties, resolving placeholders,
 * or interpreting configuration values.
 * <p>
 * A TypeMapper can be obtained in two ways. The shared global instance is accessible via {@link #getInstance()} and
 * comes pre-configured with a standard set of converters that cover most day-to-day conversions. If you need full
 * isolation, create a fresh mapper using {@link #newPreconfigured()} or {@link #newEmpty()} and register only the
 * converters you want. WireDI's Environment keeps its own mapper instance, typically exposed via an environment accessor.
 * That mapper is built from the global defaults and can be further customized during environment boot.
 * <p>
 * Conversion resolution proceeds in small, deterministic steps. If the input value already is an instance of the
 * requested target type, the original value is returned. Otherwise, the mapper looks up a converter registered for the
 * concrete source type and requested target type. If a specific converter is not available, general converters
 * for the target type are consulted in registration order. If no converter produces a value, an
 * {@link com.wiredi.runtime.types.exceptions.InvalidPropertyTypeException} is thrown to signal that a conversion was
 * requested that is unknown to the system. Primitive target classes are treated as their boxed counterparts so callers
 * may freely request either {@code int.class} or {@code Integer.class} and expect identical results.
 * <p>
 * The mapper internally caches converter lookups on a per source-type and target-type basis to avoid repeated
 * resolution overhead. This cache stores the chosen {@link TypeConverter} as well as successful conversions carried
 * out during resolver selection. Converters are expected to be stateless and thread-safe. The supplied
 * concrete converters follow this rule, and custom implementations should do the same to fit this cache and reuse
 * strategy.
 * <p>
 * Typical usage reads naturally. Convert a configuration value into a number with
 * <pre>{@code
 * Integer port = typeMapper.convert("8080", Integer.class);
 * }</pre>
 * Convert a byte array into a String with
 * <pre>{@code
 * String text = typeMapper.convert(bytes, String.class);
 * }</pre>
 * Perform a null-safe conversion using {@link #tryConvert(Object, Class)} which simply returns null when the input
 * is null rather than throwing.
 * <p>
 * Extensibility revolves around registering new {@link TypeConverter} implementations. You can register a converter
 * for a specific target type and one or more supported source types using {@link #setTypeConverter(TypeConverter)}
 * and then calling {@code forAllSupportedTypes()} or {@code forSourceType(Class)}. You may also register general
 * converters via {@link #addTypeConverter(TypeConverter)} which are consulted when there is no specific mapping for
 * a given source type. Custom converters should return null from {@link TypeConverter#convert(Object)} when they cannot
 * handle the input so the mapper may fall back to later candidates.
 * <p>
 * The TypeMapper is intentionally small in scope. It exists to bridge raw configuration data and strongly typed Java
 * APIs. It integrates with the Environment so that property reads can request a specific target type without having to
 * handcraft parsing for every primitive. Keeping conversions centralized helps maintain a single source of truth and
 * avoids subtle inconsistencies across different parts of an application.
 *
 * @see TypeConverter
 * @see TypeConverterBase
 */
public final class TypeMapper {

    private static final Value<TypeMapper> INSTANCE = Value.async(() -> {
        TypeMapper typeMapper = new TypeMapper();
        preconfigure(typeMapper);
        return typeMapper;
    });

    /**
     * Configures a TypeMapper with the standard set of type converters.
     *
     * @param typeMapper The TypeMapper to configure
     * @return The configured TypeMapper
     */
    private static TypeMapper preconfigure(TypeMapper typeMapper) {
        typeMapper.setTypeConverter(BooleanTypeConverter.INSTANCE).forAllSupportedTypes();
        typeMapper.setTypeConverter(ByteArrayTypeConverter.INSTANCE).forAllSupportedTypes();
        typeMapper.setTypeConverter(CharSequenceTypeConverter.INSTANCE).forAllSupportedTypes();
        typeMapper.setTypeConverter(ClassTypeConverter.INSTANCE).forAllSupportedTypes();
        typeMapper.setTypeConverter(DoubleTypeConverter.INSTANCE).forAllSupportedTypes();
        typeMapper.setTypeConverter(DurationTypeConverter.INSTANCE).forAllSupportedTypes();
        typeMapper.setTypeConverter(FloatTypeConverter.INSTANCE).forAllSupportedTypes();
        typeMapper.setTypeConverter(IntTypeConverter.INSTANCE).forAllSupportedTypes();
        typeMapper.setTypeConverter(LongTypeConverter.INSTANCE).forAllSupportedTypes();
        typeMapper.setTypeConverter(ShortTypeConverter.INSTANCE).forAllSupportedTypes();
        typeMapper.setTypeConverter(StringTypeConverter.INSTANCE).forAllSupportedTypes();
        return typeMapper;
    }

    private final TypeMap<List<TypeConverter<?>>> generalConverters = new TypeMap<>();
    private final TypeMap<TypeMap<@Nullable TypeConverter<?>>> typedTypeConverters = new TypeMap<>();

    /**
     * Creates a new empty TypeMapper with no converters.
     */
    public TypeMapper() {
    }

    /**
     * Creates a new TypeMapper with the specified converters.
     *
     * @param generalConverters   The general converters to use
     * @param typedTypeConverters The typed converters to use
     */
    public TypeMapper(
            TypeMap<List<TypeConverter<?>>> generalConverters,
            TypeMap<TypeMap<@Nullable TypeConverter<?>>> typedTypeConverters
    ) {
        this.generalConverters.putAll(generalConverters);
        this.typedTypeConverters.putAll(typedTypeConverters);
    }

    /**
     * Creates a new TypeMapper with the standard set of converters.
     *
     * @return A new TypeMapper with standard converters
     */
    public static TypeMapper newPreconfigured() {
        return preconfigure(new TypeMapper());
    }

    /**
     * Creates a new empty TypeMapper with no converters.
     *
     * @return A new empty TypeMapper
     */
    public static TypeMapper newEmpty() {
        return new TypeMapper();
    }

    /**
     * Gets the global instance of TypeMapper.
     * <p>
     * This instance is shared across the application and is preconfigured
     * with the standard set of converters.
     *
     * @return The global TypeMapper instance
     */
    public static TypeMapper getInstance() {
        return INSTANCE.get();
    }

    /**
     * Copies all type converters from another TypeMapper into this one.
     * <p>
     * This method clears all existing converters in this TypeMapper before
     * copying the converters from the provided TypeMapper.
     *
     * @param typeMapper The TypeMapper to copy converters from
     */
    public void takeTypeConvertersFrom(TypeMapper typeMapper) {
        this.generalConverters.clear();
        this.typedTypeConverters.clear();

        this.generalConverters.putAll(typeMapper.generalConverters);
        this.typedTypeConverters.putAll(typeMapper.typedTypeConverters);
    }

    /**
     * Registers a type converter for a specific type.
     * <p>
     * This method registers the converter to handle conversions from the specified
     * type to all target types supported by the converter.
     *
     * @param converter The converter to register
     * @param type      The source type to register the converter for
     * @param <T>       The target type that the converter produces
     */
    public <T> void setTypeConverterTo(TypeConverter<T> converter, Class<?> type) {
        converter.getTargetTypes().forEach(sourceType -> typedTypeConverters.computeIfAbsent(type, TypeMap::new).put(sourceType, converter));
    }

    /**
     * Starts the fluent API for registering a type converter.
     * <p>
     * This method returns a {@link SetTypeConverterStage} that can be used to
     * specify which source types the converter should handle.
     *
     * @param converter The converter to register
     * @param <T>       The target type that the converter produces
     * @return A stage for configuring the converter
     */
    @NotNull
    public <T> SetTypeConverterStage<T> setTypeConverter(TypeConverter<T> converter) {
        return new SetTypeConverterStage<>(converter, typedTypeConverters);
    }

    /**
     * Adds a general type converter.
     * <p>
     * General converters are consulted when no specific converter is found for a type.
     * They are tried in the order they were added.
     *
     * @param converter The converter to add
     * @param <T>       The target type that the converter produces
     */
    public <T> void addTypeConverter(TypeConverter<T> converter) {
        converter.getTargetTypes().forEach(sourceType -> generalConverters.computeIfAbsent((sourceType), ArrayList::new).add(converter));
    }

    /**
     * Attempts to convert an object to the specified type, returning null if the object is null.
     * <p>
     * This method is a null-safe wrapper around {@link #convert(Object, Class)}.
     * If the input object is null, this method returns null without attempting conversion.
     *
     * @param object The object to convert, may be null
     * @param type   The target type to convert to
     * @param <T>    The generic target type
     * @return The converted object, or null if the input was null
     */
    @Nullable
    public <T> T tryConvert(
            @Nullable Object object,
            @NotNull Class<T> type
    ) {
        if (object == null) {
            return null;
        } else {
            return convert(object, type);
        }
    }

    /**
     * Converts an object to the specified type.
     * <p>
     * This method attempts to convert the input object to the target type using
     * the registered type converters. If the object is already an instance of the
     * target type, it is returned as-is.
     * <p>
     * The conversion process follows these steps:
     * 1. If the object is already an instance of the target type, return it
     * 2. Look for a specific converter registered for the object's type and target type
     * 3. If no specific converter is found, try general converters
     * 4. If no converter can convert the object, throw an exception
     *
     * @param object The object to convert, must not be null
     * @param type   The target type to convert to
     * @param <T>    The generic target type
     * @return The converted object
     * @throws InvalidPropertyTypeException If the object cannot be converted to the target type
     */
    @NotNull
    public <T> T convert(
            @NotNull Object object,
            @NotNull Class<T> type
    ) throws InvalidPropertyTypeException {
        Class<T> targetType = Primitives.tryBox(type);
        if (targetType.isAssignableFrom(object.getClass())) {
            return (T) object;
        }

        TypeMap<TypeConverter<?>> innerTypeMap = typedTypeConverters.computeIfAbsent(object.getClass(), TypeMap::new);
        AtomicReference<T> result = new AtomicReference<>();
        TypeConverter<?> typeConverter = innerTypeMap.computeIfAbsent(targetType, () -> determineTypeConverter(object, targetType, result));

        if (result.get() == null && typeConverter != null) {
            result.set((T) typeConverter.convert(object));
        }

        T t = result.get();
        if (t == null) {
            throw new InvalidPropertyTypeException(object, targetType);
        }

        return t;
    }

    /**
     * Determines the appropriate type converter for converting an object to a target type.
     * <p>
     * This method tries to find a converter that can convert the given object to the target type.
     * It first checks if the target type is an enum, in which case it uses an EnumTypeConverter.
     * If not, it tries all general converters registered for the target type.
     * <p>
     * If a converter is found and successfully converts the object, the result is stored in the
     * provided AtomicReference and the converter is returned.
     *
     * @param object     The object to convert
     * @param targetType The target type to convert to
     * @param result     An AtomicReference to store the conversion result
     * @param <T>        The generic target type
     * @return The converter that successfully converted the object, or null if no converter was found
     */
    @Nullable
    private <T> TypeConverter<T> determineTypeConverter(
            @NotNull Object object,
            @NotNull Class<T> targetType,
            @NotNull AtomicReference<T> result
    ) {
        if (targetType.isEnum()) {
            EnumTypeConverter<?> typeConverter = EnumTypeConverter.createFor(targetType);
            T converted = (T) typeConverter.convert(object);
            if (converted != null) {
                result.set(converted);
                return (TypeConverter<T>) typeConverter;
            } else {
                return null;
            }
        }
        List<TypeConverter<?>> converters = generalConverters.get(targetType);
        if (converters != null) {
            for (TypeConverter<?> converter : converters) {
                if (converter.supports(targetType)) {
                    Object converted = converter.convert(object);
                    if (converted != null) {
                        result.set((T) converted);
                        return (TypeConverter<T>) converter;
                    }
                }
            }
        }

        return null;
    }

    /**
     * A builder-style class for configuring type converters in the TypeMapper.
     * <p>
     * This class is part of the fluent API for registering type converters.
     * It allows specifying which source types a converter should handle,
     * either individually through {@link #forSourceType(Class)} or
     * automatically for all supported types through {@link #forAllSupportedTypes()}.
     *
     * @param <T> The target type that the converter produces
     */
    public static class SetTypeConverterStage<T> {

        private final TypeConverter<T> converter;
        private final TypeMap<TypeMap<@Nullable TypeConverter<?>>> targetTypeMap;

        /**
         * Creates a new stage for configuring a type converter.
         *
         * @param converter     The type converter to configure
         * @param targetTypeMap The map where the converter will be registered
         */
        public SetTypeConverterStage(TypeConverter<T> converter, TypeMap<TypeMap<@Nullable TypeConverter<?>>> targetTypeMap) {
            this.converter = converter;
            this.targetTypeMap = targetTypeMap;
        }

        /**
         * Registers the converter for a specific source type.
         * <p>
         * This method will register the converter to handle conversions from the specified
         * source type to all target types supported by the converter.
         *
         * @param sourceType The source type to register the converter for
         * @return This stage instance for method chaining
         * @throws IllegalStateException If the source type is already mapped to a different converter
         */
        public SetTypeConverterStage<T> forSourceType(Class<?> sourceType) {
            Class<?> source = Primitives.tryBox(sourceType);

            TypeMap<@Nullable TypeConverter<?>> targetTypeConverters = targetTypeMap.computeIfAbsent(source, TypeMap::new);
            converter.getTargetTypes().forEach(targetType -> {
                TypeConverter<?> typeConverter = targetTypeConverters.get(targetType);
                if (typeConverter != null) {
                    if (typeConverter == converter) {
                        return;
                    }
                    throw new IllegalStateException("Target type " + source + " is already mapped to " + typeConverter);
                }

                targetTypeConverters.put(targetType, converter);
            });
            return this;
        }

        /**
         * Registers the converter for all source types it supports.
         * <p>
         * This method uses the {@link TypeConverter#supportedSources()} method to determine
         * which source types the converter can handle, and registers it for all of them.
         */
        public void forAllSupportedTypes() {
            converter.supportedSources().forEach(this::forSourceType);
        }
    }
}
