package com.wiredi.runtime.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * An interface for cache abstraction.
 * <p>
 * Implementations must adhere to the annotations, but may implement any caching strategy they want.
 */
public interface Cache<K, V> {

    Cache<K, V> put(@Nullable K k, @NotNull V v);

    @NotNull Optional<V> get(@Nullable K k);

    @NotNull
    default Optional<V> get(@Nullable K k, Supplier<Optional<V>> defaultValue) {
        return get(k).or(() -> {
            Optional<V> v = defaultValue.get();
            v.ifPresent(it -> put(k, it));
            return v;
        });
    }

    @NotNull
    default V getOr(@Nullable K k, Supplier<V> defaultValue) {
        return get(k).orElseGet(() -> {
            V v = defaultValue.get();
            put(k, v);
            return v;
        });
    }

    int size();

    Cache<K, V> invalidate();

    Cache<K, V> invalidate(@Nullable K k);

}
