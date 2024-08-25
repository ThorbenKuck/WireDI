package com.wiredi.runtime.types.converter;

import com.wiredi.runtime.types.AbstractTypeConverter;
import com.wiredi.runtime.types.Bytes;

import java.util.List;

public class FloatTypeConverter extends AbstractTypeConverter<Float> {

    public static final FloatTypeConverter INSTANCE = new FloatTypeConverter();

    public FloatTypeConverter() {
        super(List.of(Float.class, float.class));
    }

    @Override
    protected void setup() {
        register(String.class, Float::parseFloat);
        register(byte[].class, Bytes::toFloat);

        register(long.class, Long::floatValue);
        register(short.class, Short::floatValue);
        register(int.class, Integer::floatValue);
        register(double.class, Double::floatValue);

        register(Long.class, Long::floatValue);
        register(Short.class, Short::floatValue);
        register(Integer.class, Integer::floatValue);
        register(Double.class, Double::floatValue);
    }
}
