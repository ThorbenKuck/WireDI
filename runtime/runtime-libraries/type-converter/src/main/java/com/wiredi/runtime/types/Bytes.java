package com.wiredi.runtime.types;

import com.wiredi.runtime.collections.EnumSet;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Bytes {

    private final byte[] content;

    public Bytes(byte[] content) {
        this.content = content;
    }

    public static byte[] convert(boolean b) {
        return ByteBuffer.wrap(Boolean.toString(b).getBytes()).array();
    }

    public static byte[] convert(short i) {
        return ByteBuffer.allocate(Short.BYTES).putShort(i).array();
    }

    public static byte[] convert(int i) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(i).array();
    }

    public static byte[] convert(long i) {
        return ByteBuffer.allocate(Long.BYTES).putLong(i).array();
    }

    public static byte[] convert(float i) {
        return ByteBuffer.allocate(Float.BYTES).putFloat(i).array();
    }

    public static byte[] convert(double i) {
        return ByteBuffer.allocate(Double.BYTES).putDouble(i).array();
    }

    public static byte[] convert(String s) {
        return convert(s, Charset.defaultCharset());
    }

    public static byte[] convert(String s, Charset charset) {
        return ByteBuffer.wrap(s.getBytes(charset)).array();
    }

    public static byte[] convert(Enum<?> e) {
        return convert(e.name());
    }

    public static Bytes of(boolean b) {
        return new Bytes(convert(b));
    }

    public static Bytes of(short i) {
        return new Bytes(convert(i));
    }

    public static Bytes of(int i) {
        return new Bytes(convert(i));
    }

    public static Bytes of(long i) {
        return new Bytes(convert(i));
    }

    public static Bytes of(float i) {
        return new Bytes(convert(i));
    }

    public static Bytes of(double i) {
        return new Bytes(convert(i));
    }

    public static Bytes of(String s) {
        return new Bytes(convert(s));
    }

    public static Bytes of(String s, Charset charset) {
        return new Bytes(convert(s, charset));
    }

    public static Bytes of(Enum<?> e) {
        return new Bytes(convert(e));
    }

    public static boolean toBoolean(byte[] bytes) {
        return Boolean.parseBoolean(toString(bytes));
    }

    public static short toShort(byte[] bytes) {
        if (bytes.length == Short.BYTES) {
            ByteBuffer allocate = ByteBuffer.allocate(Short.BYTES);
            allocate.put(bytes);
            allocate.flip();
            return allocate.getShort();
        } else {
            return Short.parseShort(toString(bytes));
        }
    }

    public static int toInt(byte[] bytes) {
        if (bytes.length == Integer.BYTES) {
            ByteBuffer allocate = ByteBuffer.allocate(Integer.BYTES);
            allocate.put(bytes);
            allocate.flip();
            return allocate.getInt();
        } else {
            return Integer.parseInt(toString(bytes));
        }
    }

    public static long toLong(byte[] bytes) {
        if (bytes.length == Long.BYTES) {
            ByteBuffer allocate = ByteBuffer.allocate(Long.BYTES);
            allocate.put(bytes);
            allocate.flip();
            return allocate.getLong();
        } else {
            return Long.parseLong(toString(bytes));
        }
    }

    public static float toFloat(byte[] bytes) {
        if (bytes.length == Float.BYTES) {
            ByteBuffer allocate = ByteBuffer.allocate(Float.BYTES);
            allocate.put(bytes);
            allocate.flip();
            return allocate.getFloat();
        } else {
            return Float.parseFloat(toString(bytes));
        }
    }

    public static double toDouble(byte[] bytes) {
        if (bytes.length == Double.BYTES) {
            ByteBuffer allocate = ByteBuffer.allocate(Double.BYTES);
            allocate.put(bytes);
            allocate.flip();
            return allocate.getDouble();
        } else {
            return Double.parseDouble(toString(bytes));
        }
    }

    public static String toString(byte[] bytes) {
        return toString(bytes, Charset.defaultCharset());
    }

    public static String toString(byte[] bytes, Charset charset) {
        return new String(bytes, charset);
    }

    public static <T extends Enum<T>> T toEnum(byte[] bytes, Class<T> type) {
        String name = toString(bytes);
        return EnumSet.of(type).require(name);
    }

    public boolean toBoolean() {
        return toBoolean(content);
    }

    public short toShort() {
        return toShort(content);
    }

    public int toInt() {
        return toInt(content);
    }

    public long toLong() {
        return toLong(content);
    }

    public float toFloat() {
        return toFloat(content);
    }

    public double toDouble() {
        return toDouble(content);
    }

    public String toString() {
        return toString(content);
    }

    public String toString(Charset charset) {
        return toString(content, charset);
    }

    public <T extends Enum<T>> T toEnum(Class<T> type) {
        return toEnum(content, type);
    }
}
