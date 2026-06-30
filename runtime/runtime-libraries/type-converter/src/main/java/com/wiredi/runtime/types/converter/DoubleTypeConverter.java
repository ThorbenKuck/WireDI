package com.wiredi.runtime.types.converter;

import com.wiredi.runtime.types.TypeConverterBase;
import com.wiredi.runtime.types.Bytes;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Converts values to {@link Double} or {@code double}.
 *
 * Supports parsing from text and widening from numeric primitives. Part of the default
 * {@link com.wiredi.runtime.types.TypeMapper} preconfiguration and exposed as a stateless
 * singleton via {@link #INSTANCE}.
 */
public class DoubleTypeConverter extends TypeConverterBase<Double> {

    public static final DoubleTypeConverter INSTANCE = new DoubleTypeConverter();

    public DoubleTypeConverter() {
        super(List.of(Double.class, double.class));
    }

    @Override
    protected void setup() {
        register(String.class, Double::parseDouble);
        register(byte[].class, Bytes::toDouble);
        register(ByteBuffer.class, buffer -> Bytes.toDouble(buffer.array()));

        register(long.class, Long::doubleValue);
        register(short.class, Short::doubleValue);
        register(int.class, Integer::doubleValue);
        register(float.class, Float::doubleValue);

        register(Long.class, Long::doubleValue);
        register(Short.class, Short::doubleValue);
        register(Integer.class, Integer::doubleValue);
        register(Float.class, Float::doubleValue);
    }
}
