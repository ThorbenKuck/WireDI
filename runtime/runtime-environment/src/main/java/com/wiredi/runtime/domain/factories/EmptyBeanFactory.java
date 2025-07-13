package com.wiredi.runtime.domain.factories;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.BeanFactory;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public record EmptyBeanFactory<T>(
        @Nullable TypeIdentifier<T> rootType
) implements BeanFactory<T> {

    public static final EmptyBeanFactory INSTANCE = new EmptyBeanFactory(null);

    @Override
    public @NotNull TypeIdentifier<T> rootType() {
        if (rootType == null) {
            throw new UnsupportedOperationException("Empty bean factory does not support root type");
        } else {
            return rootType;
        }
    }

    @Override
    public @NotNull Collection<Bean<T>> getAll(@NotNull WireContainer wireRepository) {
        return Collections.emptyList();
    }

    @Override
    public @NotNull Collection<Bean<T>> getAll(@NotNull WireContainer wireRepository, @NotNull TypeIdentifier<T> type) {
        return Collections.emptyList();
    }

    @Override
    public @Nullable Bean<T> get(@NotNull WireContainer wireRepository, @NotNull TypeIdentifier<T> type) {
        return null;
    }

    @Override
    public @Nullable Bean<T> get(@NotNull WireContainer wireRepository, @NotNull QualifiedTypeIdentifier<T> type) {
        return null;
    }

    @Override
    public void register(@NotNull IdentifiableProvider<T> identifiableProvider) {
        throw new UnsupportedOperationException("Empty bean factory does not support registration");
    }

    @Override
    public @Nullable IdentifiableProvider<T> resolveProvider(@Nullable QualifierType qualifier) {
        return null;
    }
}
