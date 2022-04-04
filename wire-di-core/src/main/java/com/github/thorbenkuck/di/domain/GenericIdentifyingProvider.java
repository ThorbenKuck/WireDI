package com.github.thorbenkuck.di.domain;

import com.github.thorbenkuck.di.WireRepository;
import org.jetbrains.annotations.NotNull;

public class GenericIdentifyingProvider<T> implements IdentifiableProvider<T> {

    @NotNull
    private final T instance;
    @NotNull
    private final Class[] types;
    @NotNull
    private final Class<?> type;

    public GenericIdentifyingProvider(@NotNull final T instance) {
        this.instance = instance;
        this.type = instance.getClass();
        this.types = new Class[]{ instance.getClass() };
    }

    @Override
    @NotNull
    public Class<?> type() {
        return type;
    }

    @Override
    @NotNull
    public Class<?>[] wiredTypes() {
        return types;
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
}
