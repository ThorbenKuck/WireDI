package com.wiredi.runtime.types.converter;

import com.wiredi.runtime.types.AbstractTypeConverter;
import com.wiredi.runtime.types.Bytes;

import java.nio.ByteBuffer;
import java.util.List;

public class BooleanTypeConverter extends AbstractTypeConverter<Boolean> {

    public static final BooleanTypeConverter INSTANCE = new BooleanTypeConverter();

    public BooleanTypeConverter() {
        super(List.of(Boolean.class, boolean.class));
    }

    @Override
    protected void setup() {
        register(String.class, Boolean::parseBoolean);
        register(byte[].class, Bytes::toBoolean);
        register(ByteBuffer.class, buffer -> Bytes.toBoolean(buffer.array()));
    }
}
