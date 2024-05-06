package com.wiredi.compiler.constructors;

import com.wiredi.runtime.values.Value;

public class SingletonTypeConstructor<T, S extends T> implements TypeConstructor<T, S> {

    private final Value<S> result = Value.empty();

    private final TypeConstructor<T, S> delegate;

    public SingletonTypeConstructor(TypeConstructor<T, S> delegate) {
        this.delegate = delegate;
    }

    @Override
    public S construct(Class<?> caller, Class<T> type) {
        return result.getOrSet(() -> delegate.construct(caller, type));
    }

    @Override
    public TypeConstructor<T, S> asSingleton() {
        return this;
    }
}
