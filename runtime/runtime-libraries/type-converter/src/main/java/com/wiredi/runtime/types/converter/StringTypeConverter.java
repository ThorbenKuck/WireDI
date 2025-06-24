package com.wiredi.runtime.types.converter;

import com.wiredi.runtime.types.TypeConverterBase;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

public class StringTypeConverter extends TypeConverterBase<String> {

    public static final StringTypeConverter INSTANCE = new StringTypeConverter();

    public StringTypeConverter() {
        super(String.class);
    }

    @Override
    protected void setup() {
        register(byte[].class, String::new);
        register(char[].class, String::new);
        register(StringBuffer.class, String::new);
        register(StringBuilder.class, String::new);
        register(ByteBuffer.class, buffer -> new String(buffer.array()));
        register(ByteArrayInputStream.class, byteArrayInputStream -> new String(byteArrayInputStream.readAllBytes()));
        register(CharSequence.class, CharSequence::toString);

        register(long.class, Object::toString);
        register(int.class, Object::toString);
        register(short.class, Object::toString);
        register(boolean.class, Object::toString);
        register(float.class, Object::toString);
        register(double.class, Object::toString);

        register(Long.class, Object::toString);
        register(Integer.class, Object::toString);
        register(Short.class, Object::toString);
        register(Boolean.class, Object::toString);
        register(Float.class, Object::toString);
        register(Double.class, Object::toString);

        register(String.class, s -> s);
    }
}
