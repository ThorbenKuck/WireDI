package com.wiredi.runtime.converter;

import com.wiredi.runtime.types.AbstractTypeConverter;
import com.wiredi.runtime.types.Bytes;

import java.time.Instant;

public class InstantTypeConverter extends AbstractTypeConverter<Instant> {

    public InstantTypeConverter() {
        super(Instant.class);
    }

    @Override
    protected void setup() {
        register(byte[].class, bytes -> {
            try {
                return Instant.ofEpochMilli(Bytes.toLong(bytes));
            } catch (Throwable t) {
                return Instant.parse(Bytes.toString(bytes));
            }
        });

        register(String.class, Instant::parse);
        register(Long.class, Instant::ofEpochSecond);
        register(Integer.class, i -> Instant.ofEpochMilli(i));
        register(Short.class, Instant::ofEpochSecond);
    }
}
