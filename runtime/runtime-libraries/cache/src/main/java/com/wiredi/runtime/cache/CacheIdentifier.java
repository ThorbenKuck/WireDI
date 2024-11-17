package com.wiredi.runtime.cache;

public record CacheIdentifier<K, V>(
        Class<K> keyType,
        Class<V> valueType,
        Object identifier
) {
}
