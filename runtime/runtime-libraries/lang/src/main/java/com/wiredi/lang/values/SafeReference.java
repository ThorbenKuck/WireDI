package com.wiredi.lang.values;

import com.wiredi.lang.ThrowingFunction;
import com.wiredi.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SafeReference<T> {

    @Nullable
    private T instance;

    public SafeReference(@Nullable T t) {
        this.instance = t;
    }

    public SafeReference() {
        this(null);
    }

    public static <T> SafeReference<T> empty() {
        return of(null);
    }

    public static <T> SafeReference<T> of(@Nullable T value) {
        return new SafeReference<>(value);
    }

    @Nullable
    public T get() {
        return instance;
    }

    @NotNull
    public <E extends RuntimeException> T getOrThrow(Supplier<E> supplier) {
        T t = instance;

        if (t == null) {
            throw supplier.get();
        }

        return t;
    }

    public void set(@Nullable T t) {
        this.instance = t;
    }

    @NotNull
    public IfPresentStage ifPresent(@NotNull Consumer<T> presentConsumer) {
        T t = instance;
        if (t == null) {
            return IfPresentStage.wasMissing();
        } else {
            presentConsumer.accept(t);
            return IfPresentStage.wasPresent();
        }
    }

    public void ifEmpty(@NotNull Runnable runnable) {
        T t = instance;
        if (t == null) {
            runnable.run();
        }
    }

    public boolean isPresent() {
        T t = instance;
        return t != null;
    }

    public boolean isEmpty() {
        return !isPresent();
    }

    @Nullable
    public T updateIfPresent(@NotNull Function<T, T> function) {
        T t = instance;
        if (t != null) {
            T value = function.apply(t);
            if (value != null) {
                instance = value;
            }
        }
        return t;
    }

    @Nullable
    public T update(@NotNull Function<T, T> function) {
        T t = instance;
        if (t != null) {
            instance = function.apply(t);
        }
        return t;
    }

    @NotNull
    public T getOrSet(Supplier<@NotNull T> supplier) {
        T t = instance;
        if (t != null) {
            return t;
        }
        T value = supplier.get();
        instance = value;
        return value;
    }

    @Nullable
    public <S, E extends Exception> S mapIfPresent(ThrowingFunction<T, S, E> function) throws E {
        T t = instance;
        if (t != null) {
            return function.apply(t);
        }
        return null;
    }

    public T getAndUpdate(Function<T, T> function) {
        T t = instance;
        instance = function.apply(instance);
        return t;
    }

    public T updateAndGet(Function<T, T> function) {
        T t = function.apply(instance);
        instance = t;
        return t;
    }

    public void setIfEmpty(ThrowingSupplier<T> supplier) {
        T t = instance;
        if (t == null) {
            try {
                instance = supplier.get();
            } catch (Throwable ignored) {
            }
        }
    }
}
