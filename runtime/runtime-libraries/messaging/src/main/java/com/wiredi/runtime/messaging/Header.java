package com.wiredi.runtime.messaging;

import com.wiredi.runtime.collections.EnumSet;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static com.wiredi.runtime.lang.Preconditions.is;

/**
 * A simple record to wrap a header value.
 * <p>
 * This class provides some methods to decode these bytes.
 *
 * @param content the content of the header.
 */
public record Header(String name, byte[] content) {

    public static Header of(String name, String value) {
        return new Header(name, value.getBytes(StandardCharsets.UTF_8));
    }

    public static Header of(String name, short s) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort(s);
        buffer.flip();
        return new Header(name, buffer.array());
    }

    public static Header of(String name, int s) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(s);
        buffer.flip();
        return new Header(name, buffer.array());
    }

    public static Header of(String name, long s) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(s);
        buffer.flip();
        return new Header(name, buffer.array());
    }

    public static Header of(String name, float s) {
        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
        buffer.putFloat(s);
        buffer.flip();
        return new Header(name, buffer.array());
    }

    public static Header of(String name, double s) {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.putDouble(s);
        buffer.flip();
        return new Header(name, buffer.array());
    }

    public static Header of(String name, Enum<?> s) {
        return of(name, s.name());
    }

    public static Header of(String name, Instant instant) {
        return of(name, instant.toEpochMilli());
    }

    public static Header of(String name, Instant instant, DateTimeFormatter dateTimeFormatter) {
        return of(name, dateTimeFormatter.format(instant));
    }

    public String decodeToString(Charset charset) {
        return new String(content, charset);
    }

    public String decodeToString() {
        return new String(content, StandardCharsets.UTF_8);
    }

    public short decodeToShort() {
        is(content.length != Short.BYTES, () -> "Cannot convert bytes " + Arrays.toString(content) + " to Short");
        ByteBuffer buffer = ByteBuffer.wrap(content);
        return buffer.getShort();
    }

    public int decodeToInt() {
        is(content.length != Integer.BYTES, () -> "Cannot convert bytes " + Arrays.toString(content) + " to Integer");
        ByteBuffer buffer = ByteBuffer.wrap(content);
        return buffer.getInt();
    }

    public long decodeToLong() {
        is(content.length != Long.BYTES, () -> "Cannot convert bytes " + Arrays.toString(content) + " toLong");
        ByteBuffer buffer = ByteBuffer.wrap(content);
        return buffer.getLong();
    }

    public float decodeToFloat() {
        is(content.length != Float.BYTES, () -> "Cannot convert bytes " + Arrays.toString(content) + " to Float");
        ByteBuffer buffer = ByteBuffer.wrap(content);
        return buffer.getFloat();
    }

    public double decodeToDouble() {
        is(content.length != Double.BYTES, () -> "Cannot convert bytes " + Arrays.toString(content) + " to Double");
        ByteBuffer buffer = ByteBuffer.wrap(content);
        return buffer.getDouble();
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
        String name = decodeToString();
        return EnumSet.of(clazz).require(name);
    }
}
