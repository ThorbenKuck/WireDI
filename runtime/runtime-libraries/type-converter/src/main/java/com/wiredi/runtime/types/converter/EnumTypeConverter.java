package com.wiredi.runtime.types.converter;

import com.wiredi.runtime.collections.EnumSet;
import com.wiredi.runtime.types.TypeConverterBase;
import com.wiredi.runtime.types.Bytes;

import java.nio.ByteBuffer;

public class EnumTypeConverter<T extends Enum<T>> extends TypeConverterBase<T> {

    private final EnumSet<T> values;

    public EnumTypeConverter(Class<T> enumClass) {
        super(enumClass);
        this.values = EnumSet.of(enumClass);
    }

    public static <T extends Enum<T>> EnumTypeConverter<T> createFor(Class<?> enumClass) {
        if (!enumClass.isEnum()) {
            throw new IllegalArgumentException(enumClass + " is not an enum");
        }

        return new EnumTypeConverter<>((Class<T>) enumClass);
    }

    // These lines should not be a method reference.
    // It is important, as the EnumSet is not present when this method is invoked.
    // Putting down method references would result in a NullPointerException.
    @SuppressWarnings("Convert2MethodRef")
    @Override
    protected void setup() {
        register(String.class, string -> values.requireIgnoreCase(string));
        register(byte[].class, bytes -> values.requireIgnoreCase(Bytes.toString(bytes)));
        register(ByteBuffer.class, buffer -> values.requireIgnoreCase(new String(buffer.array())));
    }
}
