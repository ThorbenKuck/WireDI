package com.wiredi.runtime.domain.scopes.cache;

import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A store for {@link Bean} instances.
 * <p>
 * Such a store is used to cache instances of {@link Bean}s created in a {@link com.wiredi.runtime.domain.Scope}.
 * Stores can be understood as maps, holding {@link Bean} instances keyed by their unqualified {@link TypeIdentifier}.
 */
public interface ScopeStore {

    static SingeltonScopeStore threadSafe() {
        return new SingeltonScopeStore(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
    }

    @NotNull <T> Bean<T> getOrSet(
            @NotNull IdentifiableProvider<T> provider,
            @NotNull TypeIdentifier<?> type,
            @NotNull Supplier<@NotNull Bean<T>> instanceFactory
    );

    @NotNull <T> Optional<Bean<T>> getOrTrySet(
            @NotNull IdentifiableProvider<T> provider,
            @NotNull TypeIdentifier<?> type,
            @NotNull Supplier<@NotNull Optional<Bean<T>>> instanceFactory
    );

    void tearDown();

    <T> Collection<Bean<T>> getAll(TypeIdentifier<T> type, Supplier<Collection<Bean<T>>> supplier);

    boolean contains(QualifiedTypeIdentifier<?> type);

    boolean contains(TypeIdentifier<?> type);
}
