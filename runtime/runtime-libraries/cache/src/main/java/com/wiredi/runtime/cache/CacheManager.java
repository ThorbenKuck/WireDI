package com.wiredi.runtime.cache;

import org.jetbrains.annotations.NotNull;

public interface CacheManager {
    <K, V> @NotNull Cache<K, V> getCache(@NotNull Object cacheIdentifier);
}
