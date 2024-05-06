package com.wiredi.runtime.beans.value;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.exceptions.BeanNotFoundException;
import com.wiredi.runtime.exceptions.DiInstantiationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class FoundBeanValue<T> implements BeanValue<T> {

    @Nullable
    private final IdentifiableProvider<T> provider;

    public FoundBeanValue(@Nullable IdentifiableProvider<T> provider) {
        this.provider = provider;
    }

    @Override
    public BeanValue<T> orElseGet(BeanValue<T> other) {
        if (provider == null) {
            return other;
        }
        return this;
    }

    @Override
    public T instantiate(WireRepository repository, TypeIdentifier<T> concreteType) {
        if (provider == null) {
            throw new BeanNotFoundException(concreteType, repository);
        }

        try {
            return provider.get(repository, concreteType);
        } catch (final Exception e) {
            throw wireCreationError(e, concreteType, provider);
        }
    }

    @Override
    public <S extends Throwable> IdentifiableProvider<T> orElseThrow(Supplier<S> throwableSupplier) throws S {
        if (provider == null) {
            throw throwableSupplier.get();
        }
        return provider;
    }

    @Override
    public boolean isPresent() {
        return provider != null;
    }

    @NotNull
    private DiInstantiationException wireCreationError(
            @NotNull final Exception e,
            @NotNull final TypeIdentifier<?> wireType,
            @NotNull final IdentifiableProvider<?> provider
    ) {
        return new DiInstantiationException("Error while wiring " + provider.type(), wireType, e);
    }
}
