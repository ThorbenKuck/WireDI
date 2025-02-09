package com.wiredi.runtime.messaging;

import com.wiredi.runtime.types.Bytes;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

/**
 * A simple record to wrap a header value.
 * <p>
 * This class provides some methods to decode these bytes.
 *
 * @param content the content of the header.
 */
public record MessageHeader(String name, byte[] content) {

    public static MessageHeader of(String name, String value) {
        return new MessageHeader(name, Bytes.convert(value));
    }

    public static MessageHeader of(String name, short s) {
        return new MessageHeader(name, Bytes.convert(s));
    }

    public static MessageHeader of(String name, int s) {
        return new MessageHeader(name, Bytes.convert(s));
    }

    public static MessageHeader of(String name, boolean s) {
        return new MessageHeader(name, Bytes.convert(s));
    }

    public static MessageHeader of(String name, long s) {
        return new MessageHeader(name, Bytes.convert(s));
    }

    public static MessageHeader of(String name, float s) {
        return new MessageHeader(name, Bytes.convert(s));
    }

    public static MessageHeader of(String name, double s) {
        return new MessageHeader(name, Bytes.convert(s));
    }

    public static MessageHeader of(String name, Enum<?> s) {
        return of(name, s.name());
    }

    public static MessageHeader of(String name, Instant instant) {
        return of(name, instant.toEpochMilli());
    }

    public static MessageHeader of(String name, Instant instant, DateTimeFormatter dateTimeFormatter) {
        return of(name, dateTimeFormatter.format(instant));
    }

    public String decodeToString(Charset charset) {
        return Bytes.toString(content, charset);
    }

    public String decodeToString() {
        return new String(content, Charset.defaultCharset());
    }

    public short decodeToShort() {
        return Bytes.toShort(content);
    }

    public int decodeToInt() {
        return Bytes.toInt(content);
    }

    public long decodeToLong() {
        return Bytes.toLong(content);
    }

    public float decodeToFloat() {
        return Bytes.toFloat(content);
    }

    public double decodeToDouble() {
        return Bytes.toDouble(content);
    }

    public Instant decodeToInstant() {
        return decodeToInstant(DateTimeFormatter.ISO_INSTANT);
    }

    public Instant decodeToInstant(DateTimeFormatter dateTimeFormatter) {
        if (content.length == Long.BYTES) {
            long epochMillis = decodeToLong();
            return Instant.ofEpochMilli(epochMillis);
        } else {
            String text = decodeToString();
            return dateTimeFormatter.parse(text, Instant::from);
        }
    }

    public <T extends Enum<T>> T decodeToEnum(Class<T> clazz) {
        return Bytes.toEnum(content, clazz);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MessageHeader that)) return false;
        return Objects.equals(name, that.name) && Objects.deepEquals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, Arrays.hashCode(content));
    }
}
