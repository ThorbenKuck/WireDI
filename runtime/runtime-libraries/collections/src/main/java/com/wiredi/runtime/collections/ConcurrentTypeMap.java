package com.wiredi.runtime.collections;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe variant of {@link TypeMap} backed by a {@link ConcurrentHashMap}.
 * It is suitable for concurrent reads and writes from multiple threads without external synchronization.
 * <p>
 * All semantics from TypeMap apply; only the backing map differs.
 * <p>
 * Example:
 * <pre>{@code
 * ConcurrentTypeMap<Object> cache = new ConcurrentTypeMap<>();
 * cache.computeIfAbsent(String.class, () -> expensiveLoad());
 * }</pre>
 *
 * @param <T> the value type
 */
public class ConcurrentTypeMap<T> extends TypeMap<T> {

    /**
     * Creates an empty ConcurrentTypeMap with the given initial capacity.
     */
    public ConcurrentTypeMap(int initialCapacity) {
        super(new ConcurrentHashMap<>(initialCapacity));
    }

    /**
     * Creates an empty ConcurrentTypeMap with the given initial capacity and load factor.
     */
    public ConcurrentTypeMap(int initialCapacity, float loadFactor) {
        super(new ConcurrentHashMap<>(initialCapacity, loadFactor));
    }

    /**
     * Creates an empty ConcurrentTypeMap with the given capacity, load factor and concurrency level.
     */
    public ConcurrentTypeMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        super(new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel));
    }

    /**
     * Copy-constructor creating a concurrent map with the same contents as the given map.
     */
    public ConcurrentTypeMap(TypeMap<T> other) {
        super(new ConcurrentHashMap<>(other.contents));
    }

    /**
     * Creates an empty ConcurrentTypeMap.
     */
    public ConcurrentTypeMap() {
        super(new ConcurrentHashMap<>());
    }

    /**
     * Creates a ConcurrentTypeMap initialized with the given contents.
     */
    public ConcurrentTypeMap(Map<String, T> contents) {
        super(new ConcurrentHashMap<>(contents));
    }
}
