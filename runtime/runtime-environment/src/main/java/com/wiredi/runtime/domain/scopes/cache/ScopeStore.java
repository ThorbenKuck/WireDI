package com.wiredi.runtime.domain.scopes.cache;

import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public interface ScopeStore {

    static SingeltonScopeStore threadSafe() {
        return new SingeltonScopeStore(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
    }

    @Nullable <T> Bean<T> getOrSet(@NotNull QualifiedTypeIdentifier<T> type, @NotNull Function<QualifiedTypeIdentifier<T>, Bean<T>> instance);

    @NotNull <T> Optional<Bean<T>> getOrTrySet(@NotNull QualifiedTypeIdentifier<T> type, @NotNull Function<QualifiedTypeIdentifier<T>, Optional<Bean<T>>> instance);

    void tearDown();

    <T> Collection<T> getAll(TypeIdentifier<T> type);
}
