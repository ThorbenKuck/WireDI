package com.github.thorbenkuck.di.domain;

public class GenericIdentifyingProvider<T> implements IdentifiableProvider<T> {

    private final T instance;
    private final Class[] types;
    private final Class<?> type;

    public GenericIdentifyingProvider(T instance) {
        this.instance = instance;
        this.type = instance.getClass();
        this.types = new Class[]{ instance.getClass() };
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public Class<?>[] wiredTypes() {
        return types;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public T get(WireRepository wiredTypes) {
        return instance;
    }
}
