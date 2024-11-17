package com.wiredi.runtime.cache;

import org.jetbrains.annotations.NotNull;

/**
 * An abstraction to access Caches.
 * <p>
 * This interface allows you to access caches.
 */
public interface CacheManager {
    /**
     * Returns the cache identified by the {@code cacheIdentifier}.
     * <p>
     * If the {@link Cache} does not exist for this {@link CacheIdentifier},
     * this CacheManager is instructed to create it.
     *
     * @param cacheIdentifier the identifier for the cache
     * @param <K> the key type
     * @param <V> the value type
     * @return the {@link Cache} related to the {@link CacheIdentifier}
     */
    <K, V> @NotNull Cache<K, V> getCache(@NotNull CacheIdentifier<K, V> cacheIdentifier);

    /**
     * Returns the cache identified by the {@code cacheIdentifier}.
     * <p>
     * If the {@link Cache} does not exist for this {@link CacheIdentifier},
     * this CacheManager is instructed to create it.
     *
     * @param keyType the type of the key
     * @param valueType the type of the value
     * @param cacheIdentifier the identifier for the cache
     * @param <K> the key type
     * @param <V> the value type
     * @return the {@link Cache} related to the {@link CacheIdentifier}
     */
    default <K, V> @NotNull Cache<K, V> getCache(Class<K> keyType, Class<V> valueType, @NotNull Object cacheIdentifier) {
        return getCache(new CacheIdentifier<>(keyType, valueType, cacheIdentifier));
    }

    /**
     * Returns the cache identified by the {@code cacheIdentifier}.
     * <p>
     * If the {@link Cache} does not exist for this {@link CacheIdentifier},
     * this CacheManager is instructed to create it.
     *
     * @param cacheIdentifier the identifier for the cache
     * @return the {@link Cache} related to the {@link CacheIdentifier}
     */
    default @NotNull Cache<Object, Object> getCache(@NotNull Object cacheIdentifier) {
        return getCache(new CacheIdentifier<>(Object.class, Object.class, cacheIdentifier));
    }
}
