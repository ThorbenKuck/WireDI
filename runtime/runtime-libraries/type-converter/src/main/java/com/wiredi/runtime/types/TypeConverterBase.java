package com.wiredi.runtime.types;

import com.wiredi.runtime.lang.ThrowingFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.function.Function;

/**
 * A convenience base class for {@link TypeConverter} implementations that maps a fixed set of
 * source types to a single target type using fast, pre-registered functions.
 *
 * Subclasses declare their target type(s) through the constructor and register all supported
 * source types inside {@link #setup()} via {@link #register(Class, com.wiredi.runtime.lang.ThrowingFunction)}.
 * When {@link #convert(Object)} is invoked, the converter looks up the function registered for the
 * concrete runtime class of the input and executes it. If no function was registered for that
 * class, this method returns null so that the {@link TypeMapper} can continue with other
 * converters. Implementations should keep registration deterministic and avoid expensive runtime
 * checks; use {@link #supports(Class)} and {@link #supportedSources()} to communicate capabilities.
 *
 * The base implementation wraps checked exceptions from registered functions into runtime
 * exceptions to keep the {@link TypeConverter} contract simple. {@link java.io.IOException}
 * becomes {@link java.io.UncheckedIOException}, other checked exceptions are rethrown as
 * {@link java.lang.reflect.UndeclaredThrowableException}. If your conversion can fail in
 * expected ways, surface that as a regular {@link RuntimeException} with a meaningful message.
 *
 * Instances are expected to be stateless and thread-safe. All built-in converters in this module
 * follow that rule and expose a single public {@code INSTANCE} for reuse.
 */
public abstract class TypeConverterBase<T> implements TypeConverter<T> {

    private final Map<Class<?>, ThrowingFunction<Object, T, Throwable>> functions = new HashMap<>();
    private final List<Class<T>> targetTypes;
    private Function<Object, Object> preHandler = Function.identity();

    public TypeConverterBase(Class<T> sourceType) {
        this(List.of(sourceType));
    }

    public TypeConverterBase(List<Class<T>> targetTypes) {
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
    public final Collection<Class<?>> supportedSources() {
        return functions.keySet();
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
