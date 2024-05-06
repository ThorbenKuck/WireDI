package com.wiredi.compiler.constructors;

import com.wiredi.runtime.values.Value;

import java.util.function.Supplier;

public class SingletonInstanceTypeConstructor<T, S extends T> implements TypeConstructor<T, S> {

    private final Value<S> result = Value.empty();

    private final Supplier<S> s;

    public SingletonInstanceTypeConstructor(Supplier<S> s) {
        this.s = s;
    }

    @Override
    public S construct(Class<?> caller, Class<T> type) {
        return result.getOrSet(s);
    }

    @Override
    public TypeConstructor<T, S> asSingleton() {
        return this;
    }
}
