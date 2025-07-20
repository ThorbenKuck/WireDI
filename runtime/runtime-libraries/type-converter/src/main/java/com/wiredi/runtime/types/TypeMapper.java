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
 * A class to convert between different types in the WireDI framework.
 * <p>
 * The goal of this class is to allow simple conversions between different types.
 * One example would be to convert Strings to Integers and vice versa.
 * <p>
 * To do so, instances of the {@link TypeConverter} interfaces are used.
 * The TypeMapper is a registry that manages these converters and provides methods to perform conversions.
 * <p>
 * This class maintains a global instance through the {@code INSTANCE} field.
 * It can be used for fast usages through the {@link #getInstance()} method.
 * Changes done to this TypeMapper will be global and take effect for every other process that uses the global instance.
 * <p>
 * If you want to use a TypeMapper and have it be completely independent of the global instance,
 * you can construct a TypeMapper using {@link #newEmpty()} or {@link #newPreconfigured()}.
 * These methods will construct a new TypeMapper instance.
 * {@link #newEmpty()} will construct a completely new TypeMapper without any {@link TypeConverter},
 * while {@link #newPreconfigured()} will include the standard set of converters.
 * <p>
 * This API is intended for simple type conversion, like converting primitives.
 * Though possible, it is not recommended to do more complex type mappings in this API.
 * If you require a "real deserialization logic", please use the messaging integration.
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
         * @param converter    The type converter to configure
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
