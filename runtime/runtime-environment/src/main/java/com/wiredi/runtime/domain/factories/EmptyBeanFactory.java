package com.wiredi.runtime.domain.factories;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.BeanFactory;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
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
    public @NotNull Collection<Bean<T>> getAll(@NotNull WireRepository wireRepository) {
        return Collections.emptyList();
    }

    @Override
    public @NotNull Collection<Bean<T>> getAll(@NotNull WireRepository wireRepository, @NotNull TypeIdentifier<T> type) {
        return Collections.emptyList();
    }

    @Override
    public @Nullable Bean<T> get(@NotNull WireRepository wireRepository) {
        return null;
    }

    @Override
    public @Nullable Bean<T> get(@NotNull WireRepository wireRepository, @NotNull TypeIdentifier<T> type) {
        return null;
    }

    @Override
    public @Nullable Bean<T> get(@NotNull WireRepository wireRepository, @NotNull QualifierType qualifier) {
        return null;
    }

    @Override
    public @Nullable Bean<T> get(@NotNull WireRepository wireRepository, @NotNull TypeIdentifier<T> type, @NotNull QualifierType qualifierType) {
        return null;
    }

    @Override
    public void register(@NotNull IdentifiableProvider<T> identifiableProvider) {
        throw new UnsupportedOperationException("Empty bean factory does not support registration");
    }
}
