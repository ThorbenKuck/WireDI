package com.wiredi.runtime.types.converter;

import com.wiredi.runtime.types.AbstractTypeConverter;
import com.wiredi.runtime.types.Bytes;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.function.Function;

public class ByteArrayTypeConverter extends AbstractTypeConverter<byte[]> {

    public static final ByteArrayTypeConverter INSTANCE = new ByteArrayTypeConverter();

    public ByteArrayTypeConverter() {
        super(byte[].class);
    }

    @Override
    protected void setup() {
        register(String.class, String::getBytes);
        register(byte[].class, b -> b);
        register(char[].class, c -> new String(c).getBytes());
        register(StringBuffer.class, b -> b.toString().getBytes());
        register(StringBuilder.class, b -> b.toString().getBytes());
        register(ByteBuffer.class, ByteBuffer::array);
        register(ByteArrayInputStream.class, ByteArrayInputStream::readAllBytes);
        register(CharSequence.class, c -> c.toString().getBytes());

        register(long.class, Bytes::convert);
        register(int.class, Bytes::convert);
        register(short.class, Bytes::convert);
        register(boolean.class, Bytes::convert);
        register(float.class, Bytes::convert);
        register(double.class, Bytes::convert);

        register(Long.class, Bytes::convert);
        register(Integer.class, Bytes::convert);
        register(Short.class, Bytes::convert);
        register(Boolean.class, Bytes::convert);
        register(Float.class, Bytes::convert);
        register(Double.class, Bytes::convert);
    }
}
