package com.wiredi.runtime.infrastructure;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.provider.AbstractIdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.values.LazyValue;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericBaseIdentifiableProvider<T> extends AbstractIdentifiableProvider<GenericBase<T>> {

    private final T t;
    private final Value<GenericBase<T>> base;

    public GenericBaseIdentifiableProvider(T t) {
        super(TypeIdentifier.of(GenericBase.class).withGeneric(t.getClass()));
        this.t = t;
        this.base = new LazyValue<>(() -> new GenericBase<>(t));
    }

    @Override
    public @Nullable GenericBase<T> get(@NotNull WireContainer wireContainer, @NotNull TypeIdentifier<GenericBase<T>> concreteType) {
        return base.get();
    }

    @Override
    public @NotNull List<TypeIdentifier<?>> additionalWireTypes() {
        List<TypeIdentifier<?>> result = new ArrayList<>();
        result.add(TypeIdentifier.of(GenericBase.class).withGeneric(t.getClass().getSuperclass()));
        result.addAll(Arrays.stream(t.getClass().getInterfaces()).map(it -> TypeIdentifier.of(GenericBase.class).withGeneric(it)).toList());
        return result;
    }

    public AbstractIdentifiableProvider<GenericBase> eraseGeneric() {
        return (AbstractIdentifiableProvider) this;
    }

    @Override
    public String toString() {
        return "IdentifiableProvider<" + type() + ">(" + base + ')';
    }
}
