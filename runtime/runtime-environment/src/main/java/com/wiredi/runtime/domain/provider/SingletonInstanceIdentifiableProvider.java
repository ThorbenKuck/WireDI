package com.wiredi.runtime.domain.provider;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.lang.Ordered;
import org.jetbrains.annotations.NotNull;

public class SingletonInstanceIdentifiableProvider<T> extends AbstractIdentifiableProvider<T> {

    @NotNull
    private final T instance;

    public SingletonInstanceIdentifiableProvider(@NotNull final T instance) {
        super((TypeIdentifier<T>) TypeIdentifier.of(instance.getClass()));
        this.instance = instance;
    }

    public <S extends T> SingletonInstanceIdentifiableProvider(@NotNull final S instance, TypeIdentifier<T> type) {
        super(type);
        this.instance = instance;
    }

    public static <T> SingletonInstanceIdentifiableProvider<T> of(@NotNull final T instance) {
        return new SingletonInstanceIdentifiableProvider<>(instance);
    }

    public static <T, S extends T> SingletonInstanceIdentifiableProvider<T> of(@NotNull final S instance, TypeIdentifier<T> type) {
        return new SingletonInstanceIdentifiableProvider<>(instance, type);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    @NotNull
    public T get(
            @NotNull final WireContainer wireContainer,
            @NotNull final TypeIdentifier<T> concreteType
    ) {
        return instance;
    }

    @Override
    public int getOrder() {
        if (instance instanceof Ordered o) {
            return o.getOrder();
        } else {
            return super.getOrder();
        }
    }

    @Override
    public String toString() {
        return "SingletonInstanceIdentifiableProvider{" + instance + '}';
    }
}