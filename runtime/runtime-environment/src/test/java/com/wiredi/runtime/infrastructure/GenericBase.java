package com.wiredi.runtime.infrastructure;

import java.util.Objects;

public class GenericBase<T> {
    private final T t;

    GenericBase(T t) {
        this.t = t;
    }

    public static <T> GenericBaseIdentifiableProvider<T> provider(T t) {
        return new GenericBaseIdentifiableProvider<>(t);
    }

    public static <T> GenericBase<T> of(T t) {
        return new GenericBase<>(t);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object.getClass().equals(t.getClass())) {
            return Objects.equals(t, object);
        }
        if ((object instanceof GenericBase<?> that)) {
            return Objects.equals(t, that.t);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(t);
    }

    @Override
    public String toString() {
        return "GenericBase<" + t.getClass().getSimpleName() + ">(" + t + ')';
    }
}
