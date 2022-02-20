package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.domain.IdentifiableProvider;
import com.github.thorbenkuck.di.domain.WireRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SingletonInstanceProvider<T> implements IdentifiableProvider<T> {

    private final T instance;

    private final int priority;

    private final Class<?> type;

    private final Class<?>[] wiredTypes;

    public SingletonInstanceProvider(T instance) {
        this(instance, DEFAULT_PRIORITY);
    }

    public SingletonInstanceProvider(T instance, int priority) {
        this(instance, priority, instance.getClass());
    }

    public SingletonInstanceProvider(T instance, int priority, Class<?> type) {
        this(instance, priority, type, estimateWiredTypes(instance));
    }

    public SingletonInstanceProvider(T instance, int priority, Class<?> type, Class<?>[] wiredTypes) {
        this.instance = instance;
        this.priority = priority;
        this.type = type;
        this.wiredTypes = wiredTypes;
    }

    private static Class<?>[] estimateWiredTypes(Object instance) {
        List<Class<?>> result = new ArrayList<>();
        result.add(instance.getClass());
        result.addAll(Arrays.asList(instance.getClass().getInterfaces()));
        result.add(instance.getClass().getSuperclass());

        return result.toArray(new Class[0]);
    }

    @Override
    public final Class<?> type() {
        return type;
    }

    @Override
    public final Class<?>[] wiredTypes() {
        return wiredTypes;
    }

    @Override
    public final boolean isSingleton() {
        return true;
    }

    @Override
    public final T get(WireRepository wiredTypes) {
        return instance;
    }

    @Override
    public final int priority() {
        return priority;
    }
}
