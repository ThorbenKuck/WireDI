package com.wiredi.runtime.domain.scopes.cache;

import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ScopeStore {

    static SingeltonScopeStore threadSafe() {
        return new SingeltonScopeStore(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
    }

    @Nullable <T> Bean<T> getOrSet(
            @NotNull IdentifiableProvider<T> provider,
            @NotNull TypeIdentifier<?> type,
            @NotNull Supplier<@Nullable Bean<T>> instanceFactory
    );

    @NotNull <T> Optional<Bean<T>> getOrTrySet(
            @NotNull IdentifiableProvider<T> provider,
            @NotNull TypeIdentifier<?> type,
            @NotNull Supplier<@NotNull Optional<Bean<T>>> instanceFactory
    );

    void tearDown();

    <T> Collection<Bean<T>> getAll(TypeIdentifier<T> type, Supplier<Collection<Bean<T>>> supplier);
}
