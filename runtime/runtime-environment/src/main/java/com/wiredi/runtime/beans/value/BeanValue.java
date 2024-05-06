package com.wiredi.runtime.beans.value;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public interface BeanValue<T> {

    BeanValue<Void> EMPTY = new FoundBeanValue<>(null);

    static <T> BeanValue<T> of(@Nullable IdentifiableProvider<T> provider) {
        return new FoundBeanValue<>(provider);
    }

    static <T> BeanValue<T> empty() {
        return (BeanValue<T>) EMPTY;
    }

    static <T> BeanValue<T> error(Throwable throwable) {
        return new BeanValueError<>(throwable);
    }

    BeanValue<T> orElseGet(BeanValue<T> beanValue);

    T instantiate(WireRepository repository, TypeIdentifier<T> concreteType);

    <S extends Throwable> IdentifiableProvider<T> orElseThrow(Supplier<S> throwableSupplier) throws S;

    boolean isPresent();

    default Optional<T> optional(WireRepository repository, TypeIdentifier<T> concreteType) {
        if (isPresent()) {
            return Optional.ofNullable(instantiate(repository, concreteType));
        } else {
            return Optional.empty();
        }
    }
}
