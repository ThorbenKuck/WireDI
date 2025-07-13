package com.wiredi.runtime.domain;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.factories.EmptyBeanFactory;
import com.wiredi.runtime.domain.factories.SimpleBeanFactory;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface BeanFactory<T> {

    static <T> BeanFactory<T> empty() {
        return EmptyBeanFactory.INSTANCE;
    }

    static <T> BeanFactory<T> empty(TypeIdentifier<T> typeIdentifier) {
        return new EmptyBeanFactory<>(typeIdentifier);
    }

    static <T> BeanFactory<T> of(TypeIdentifier<T> typeIdentifier) {
        return new SimpleBeanFactory<>(typeIdentifier);
    }

    @NotNull TypeIdentifier<T> rootType();

    @NotNull
    default Collection<Bean<T>> getAll(@NotNull WireContainer wireContainer) {
        return getAll(wireContainer, rootType());
    }

    @NotNull Collection<Bean<T>> getAll(@NotNull WireContainer wireContainer, @NotNull TypeIdentifier<T> type);

    @Nullable Bean<T> get(
            @NotNull WireContainer wireContainer,
            @NotNull TypeIdentifier<T> type
    );

    @Nullable Bean<T> get(
            @NotNull WireContainer wireContainer,
            @NotNull QualifiedTypeIdentifier<T> type
    );

    void register(@NotNull IdentifiableProvider<T> identifiableProvider);

    @Nullable
    IdentifiableProvider<T> resolveProvider(@Nullable QualifierType qualifier);
}
