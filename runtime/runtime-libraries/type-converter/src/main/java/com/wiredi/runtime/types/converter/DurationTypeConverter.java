package com.wiredi.runtime.types.converter;

import com.wiredi.runtime.types.AbstractTypeConverter;
import com.wiredi.runtime.types.TypeConverter;
import com.wiredi.runtime.types.exceptions.TypeConversionFailedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;

public class DurationTypeConverter extends AbstractTypeConverter<Duration> {

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
