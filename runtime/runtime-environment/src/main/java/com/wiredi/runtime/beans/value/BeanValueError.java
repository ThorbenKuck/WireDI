package com.wiredi.runtime.beans.value;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.exceptions.DiInstantiationException;

import java.util.function.Supplier;

public class BeanValueError<T> implements BeanValue<T> {

    private final Throwable error;

    public BeanValueError(Throwable error) {
        this.error = error;
    }

    @Override
    public BeanValue<T> orElseGet(BeanValue<T> beanValue) {
        return beanValue;
    }

    @Override
    public T instantiate(WireRepository repository, TypeIdentifier<T> concreteType) {
        if (error instanceof RuntimeException) {
            throw (RuntimeException) error;
        } else {
            throw new DiInstantiationException(error, concreteType);
        }
    }

    @Override
    public <S extends Throwable> IdentifiableProvider<T> orElseThrow(Supplier<S> throwableSupplier) throws S {
        throw throwableSupplier.get();
    }

    @Override
    public boolean isPresent() {
        return false;
    }
}
