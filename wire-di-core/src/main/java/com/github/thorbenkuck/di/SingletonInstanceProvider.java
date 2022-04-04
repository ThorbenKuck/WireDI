package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.domain.IdentifiableProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SingletonInstanceProvider<T> implements IdentifiableProvider<T> {

    @NotNull
    private final T instance;

    private final int priority;

    @NotNull
    private final Class<?> type;

    @NotNull
    private final Class<?>[] wiredTypes;

    public SingletonInstanceProvider(@NotNull final T instance) {
        this(instance, DEFAULT_PRIORITY);
    }

    public SingletonInstanceProvider(
            @NotNull final T instance,
            final int priority
    ) {
        this(instance, priority, instance.getClass());
    }

    public SingletonInstanceProvider(
            @NotNull final T instance,
            final int priority,
            @NotNull final Class<?> type
    ) {
        this(instance, priority, type, estimateWiredTypes(instance));
    }

    public SingletonInstanceProvider(
            @NotNull final T instance,
            final int priority,
            @NotNull final Class<?> type,
            @NotNull final Class<?>[] wiredTypes
    ) {
        this.instance = instance;
        this.priority = priority;
        this.type = type;
        this.wiredTypes = wiredTypes;
    }

    @NotNull
    private static Class<?>[] estimateWiredTypes(@NotNull final Object instance) {
        final List<Class<?>> result = new ArrayList<>();
        result.add(instance.getClass());
        result.addAll(Arrays.asList(instance.getClass().getInterfaces()));
        result.add(instance.getClass().getSuperclass());

        return result.toArray(new Class[0]);
    }

    @Override
    @NotNull
    public Class<?> type() {
        return type;
    }

    @Override
    @NotNull
    public Class<?>[] wiredTypes() {
        return wiredTypes;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    @NotNull
    public T get(@NotNull final WireRepository wiredTypes) {
        return instance;
    }

    @Override
    public int priority() {
        return priority;
    }
}
