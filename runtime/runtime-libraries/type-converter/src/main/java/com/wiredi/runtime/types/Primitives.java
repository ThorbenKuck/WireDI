package com.wiredi.runtime.types;

import com.wiredi.runtime.lang.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Primitives {

    private static final Map<Class<?>, Class<?>> primitiveToWrapper = Map.of(
            void.class, Void.class,
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class
    );

    private static final Map<Class<?>, Class<?>> wrapperToPrimitive = Map.of(
            Void.class, void.class,
            Boolean.class, boolean.class,
            Byte.class, byte.class,
            Character.class, char.class,
            Short.class, short.class,
            Integer.class, int.class,
            Long.class, long.class,
            Float.class, float.class,
            Double.class, double.class
    );

    @NotNull
    public static <T> Class<T> box(@NotNull Class<T> cls) {
        return (Class<T>) Preconditions.isNotNull(primitiveToWrapper.get(cls), () -> "Unable to box " + cls);
    }

    @NotNull
    public static <T> Class<T> tryBox(@NotNull Class<T> cls) {
        return (Class<T>) primitiveToWrapper.getOrDefault(cls, cls);
    }

    @NotNull
    public static <T> Class<T> unbox(@NotNull Class<T> cls) {
        return (Class<T>) Preconditions.isNotNull(wrapperToPrimitive.get(cls), () -> "Unable to box " + cls);
    }

    @NotNull
    public static <T> Class<T> tryUnbox(@NotNull Class<T> cls) {
        return (Class<T>) wrapperToPrimitive.getOrDefault(cls, cls);
    }
}
