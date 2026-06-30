package com.wiredi.runtime.types.converter;

import com.wiredi.runtime.types.TypeConverterBase;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;

/**
 * Converts values to {@link java.time.Duration}.
 *
 * This converter parses ISO-8601 duration text and can derive durations from byte-oriented inputs
 * by decoding them as UTF-8 text prior to parsing. It is included in the default
 * {@link com.wiredi.runtime.types.TypeMapper} preconfiguration and exposed as a stateless singleton
 * via {@link #INSTANCE}.
 */
public class DurationTypeConverter extends TypeConverterBase<Duration> {

    public static final DurationTypeConverter INSTANCE = new DurationTypeConverter();

    public DurationTypeConverter() {
        super(Duration.class);
    }

    @Override
    protected void setup() {
        register(String.class, Duration::parse);
        register(byte[].class, bytes -> Duration.parse(new String(bytes)));
        register(ByteBuffer.class, buffer -> Duration.parse(new String(buffer.array())));
        register(InputStream.class, buffer -> Duration.parse(new String(buffer.readAllBytes())));
    }
}
