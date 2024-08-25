package com.wiredi.runtime.types;

import com.wiredi.runtime.lang.ThrowingFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.function.Function;

public abstract class AbstractTypeConverter<T> implements TypeConverter<T> {

    private final Map<Class<?>, ThrowingFunction<Object, T, Throwable>> functions = new HashMap<>();
    private final List<Class<T>> targetTypes;
    private Function<Object, Object> preHandler = t -> t;

    public AbstractTypeConverter(Class<T> sourceType) {
        this(List.of(sourceType));
    }

    public AbstractTypeConverter(List<Class<T>> targetTypes) {
        this.targetTypes = Collections.unmodifiableList(targetTypes);
        setup();
    }

    protected <S, THROWABLE extends Throwable> void register(Class<S> sourceType, ThrowingFunction<S, T, THROWABLE> function) {
        ThrowingFunction<Object, T, Throwable> objectTFunction = functions.get(sourceType);
        if (objectTFunction != null) {
            throw new IllegalStateException("Cannot register " + sourceType.getName() + " as a type converter is already registered");
        }
        functions.put(sourceType, (ThrowingFunction<Object, T, Throwable>) function);
    }

    protected abstract void setup();

    protected void beforeConversion(Function<Object, Object> preHandler) {
        this.preHandler = preHandler;
    }

    public @Nullable T convert(@NotNull Object s) {
        ThrowingFunction<Object, T, Throwable> tFunction = functions.get(s.getClass());
        if (tFunction == null) {
            return null;
        } else {
            try {
                return tFunction.apply(preHandler.apply(s));
            } catch (Throwable e) {
                if (e instanceof RuntimeException r) {
                    throw r;
                } else if (e instanceof IOException i) {
                    throw new UncheckedIOException(i);
                } else {
                    throw new UndeclaredThrowableException(e);
                }
            }
        }
    }

    @Override
    public final List<Class<?>> supportedSources() {
        return new ArrayList<>(functions.keySet());
    }

    @Override
    public final List<Class<T>> getTargetTypes() {
        return targetTypes;
    }

    @Override
    public boolean supports(Class<?> type) {
        return functions.containsKey(type);
    }
}
