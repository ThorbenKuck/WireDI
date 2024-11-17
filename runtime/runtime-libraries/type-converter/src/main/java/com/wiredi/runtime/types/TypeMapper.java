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
 * A class to convert strings to other classes.
 * <p>
 * The goal of this class is to allow simple conversions between different types.
 * One example would be to convert Strings to Integers and vice versa.
 * <p>
 * To do so, instances of the {@link TypeConverter} interfaces are used.
 * The TypeMapper is
 * <p>
 * This class maintains a global instance through the {@code INSTANCE} field.
 * It can be used for fast usages through the {@link #getInstance()} method.
 * Changes done to this TypeMapper will be global and take effect for every other process that uses the global instance.
 * <p>
 * If you want to use a TypeMapper and have it be completely independent of the global instance,
 * you can construct a TypeMapper using {@link #newEmpty()} or {@link #newPreconfigured()}.
 * These methods will construct a new TypeMapper instance.
 * {@link #newEmpty()} will construct a completely new TypeMapper without any {@link TypeConverter}
 *
 * This api is intended for simple type conversion, like converting primitives.
 * Though possible, it is not recommended to do more complex type mappings in this api.
 * If you required a "real deserialization logic", please use the messaging integration.
 *
 * @see TypeConverter
 * @see AbstractTypeConverter
 */
public final class TypeMapper {

    private static final Value<TypeMapper> INSTANCE = Value.async(() -> {
        TypeMapper typeMapper = new TypeMapper();
        preconfigure(typeMapper);
        return typeMapper;
    });

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

    private TypeMapper() {
    }

    private TypeMapper(
            TypeMap<List<TypeConverter<?>>> generalConverters,
            TypeMap<TypeMap<@Nullable TypeConverter<?>>> typedTypeConverters
    ) {
        this.generalConverters.putAll(generalConverters);
        this.typedTypeConverters.putAll(typedTypeConverters);
    }

    public static TypeMapper newPreconfigured() {
        return preconfigure(new TypeMapper());
    }

    public static TypeMapper newEmpty() {
        return new TypeMapper();
    }

    public static TypeMapper getInstance() {
        return INSTANCE.get();
    }

    public void takeTypeConvertersFrom(TypeMapper typeMapper) {
        this.generalConverters.clear();
        this.typedTypeConverters.clear();

        this.generalConverters.putAll(typeMapper.generalConverters);
        this.typedTypeConverters.putAll(typeMapper.typedTypeConverters);
    }

    public <T> void setTypeConverterTo(TypeConverter<T> converter, Class<?> type) {
        converter.getTargetTypes().forEach(sourceType -> typedTypeConverters.computeIfAbsent(type, TypeMap::new).put(sourceType, converter));
    }

    @NotNull
    public <T> SetTypeConverterStage<T> setTypeConverter(TypeConverter<T> converter) {
        return new SetTypeConverterStage<>(converter, typedTypeConverters);
    }

    public <T> void addTypeConverter(TypeConverter<T> converter) {
        converter.getTargetTypes().forEach(sourceType -> generalConverters.computeIfAbsent((sourceType), ArrayList::new).add(converter));
    }

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

    public static class SetTypeConverterStage<T> {

        private final TypeConverter<T> converter;
        private final TypeMap<TypeMap<@Nullable TypeConverter<?>>> targetTypeMap;

        public SetTypeConverterStage(TypeConverter<T> converter, TypeMap<TypeMap<@Nullable TypeConverter<?>>> targetTypeMap) {
            this.converter = converter;
            this.targetTypeMap = targetTypeMap;
        }

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

        public void forAllSupportedTypes() {
            converter.supportedSources().forEach(this::forSourceType);
        }
    }
}
