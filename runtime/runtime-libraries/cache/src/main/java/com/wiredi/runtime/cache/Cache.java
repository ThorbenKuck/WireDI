package com.wiredi.runtime.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * An interface for cache abstraction that provides a generic way to store and retrieve data.
 * <p>
 * The Cache interface defines a contract for implementing various caching strategies while
 * maintaining a consistent API. Implementations must adhere to the annotations but may implement
 * any caching strategy they want (e.g., LRU, LFU, time-based expiration, etc.).
 * <p>
 * A Cache is typically obtained from a {@link CacheManager} using a {@link CacheIdentifier}:
 * <pre>{@code
 * // Get a cache from a CacheManager
 * CacheManager cacheManager = new InMemoryCacheManager();
 * Cache<String, User> userCache = cacheManager.getCache(
 *     new CacheIdentifier<>(String.class, User.class, "userCache")
 * );
 * 
 * // Or using the convenience method
 * Cache<String, User> userCache = cacheManager.getCache(String.class, User.class, "userCache");
 * }</pre>
 * 
 * <p>
 * Basic usage examples:
 * <pre>{@code
 * // Store a value in the cache
 * User user = new User("john.doe", "John Doe");
 * userCache.put(user.getUsername(), user);
 * 
 * // Retrieve a value from the cache
 * Optional<User> cachedUser = userCache.get("john.doe");
 * 
 * // Retrieve with a default value supplier (computed and cached if not present)
 * User user = userCache.getOr("john.doe", () -> fetchUserFromDatabase("john.doe"));
 * 
 * // Clear the entire cache
 * userCache.invalidate();
 * 
 * // Remove a specific entry
 * userCache.invalidate("john.doe");
 * }</pre>
 * 
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of values maintained by this cache
 */
public interface Cache<K, V> {

    /**
     * Adds or updates a key-value pair in the cache.
     * <p>
     * If the key already exists in the cache, its value will be updated.
     * The behavior regarding cache eviction policies when adding new entries
     * depends on the specific implementation.
     * 
     * <pre>{@code
     * // Store a user in the cache
     * User user = new User("john.doe", "John Doe");
     * cache.put(user.getUsername(), user);
     * }</pre>
     *
     * @param k the key with which the specified value is to be associated
     * @param v the value to be associated with the specified key (must not be null)
     * @return this cache instance for method chaining
     */
    Cache<K, V> put(@Nullable K k, @NotNull V v);

    /**
     * Retrieves a value from the cache by its key.
     * <p>
     * If the key is not found in the cache, an empty Optional is returned.
     * 
     * <pre>{@code
     * // Get a user from the cache
     * Optional<User> user = cache.get("john.doe");
     * if (user.isPresent()) {
     *     // Use the cached user
     *     System.out.println("Found user: " + user.get().getName());
     * } else {
     *     System.out.println("User not found in cache");
     * }
     * }</pre>
     *
     * @param k the key whose associated value is to be returned
     * @return an Optional containing the value to which the specified key is mapped,
     *         or an empty Optional if the cache contains no mapping for the key
     */
    @NotNull Optional<V> get(@Nullable K k);

    /**
     * Retrieves a value from the cache by its key, or computes and caches it if not present.
     * <p>
     * If the key is not found in the cache, the defaultValue supplier is invoked to provide
     * a value. If the supplier returns a non-empty Optional, its value is cached and returned.
     * 
     * <pre>{@code
     * // Get a user from the cache, or load from database if not present
     * Optional<User> user = cache.get("john.doe", () -> {
     *     // This will only be called if the user is not in the cache
     *     return Optional.ofNullable(userRepository.findByUsername("john.doe"));
     * });
     * }</pre>
     *
     * @param k the key whose associated value is to be returned
     * @param defaultValue a supplier that provides a default value if the key is not in the cache
     * @return an Optional containing the value from the cache or the computed default value
     */
    @NotNull
    default Optional<V> get(@Nullable K k, Supplier<Optional<V>> defaultValue) {
        return get(k).or(() -> {
            Optional<V> v = defaultValue.get();
            v.ifPresent(it -> put(k, it));
            return v;
        });
    }

    /**
     * Retrieves a value from the cache by its key, or computes and caches it if not present.
     * <p>
     * If the key is not found in the cache, the defaultValue supplier is invoked to provide
     * a value, which is then cached and returned. Unlike {@link #get(Object, Supplier)},
     * this method expects a non-null value from the supplier and returns the value directly
     * rather than an Optional.
     * 
     * <pre>{@code
     * // Get a user from the cache, or load from database if not present
     * User user = cache.getOr("john.doe", () -> {
     *     // This will only be called if the user is not in the cache
     *     User dbUser = userRepository.findByUsername("john.doe");
     *     if (dbUser == null) {
     *         // Provide a default user if not found in database
     *         return new User("john.doe", "Default User");
     *     }
     *     return dbUser;
     * });
     * }</pre>
     *
     * @param k the key whose associated value is to be returned
     * @param defaultValue a supplier that provides a default value if the key is not in the cache
     * @return the value from the cache or the computed default value
     */
    @NotNull
    default V getOr(@Nullable K k, Supplier<V> defaultValue) {
        return get(k).orElseGet(() -> {
            V v = defaultValue.get();
            put(k, v);
            return v;
        });
    }

    /**
     * Returns the number of key-value mappings in this cache.
     * <p>
     * The behavior of this method when entries expire or are evicted
     * depends on the specific implementation.
     * 
     * <pre>{@code
     * // Get the current size of the cache
     * int cacheSize = cache.size();
     * System.out.println("Cache contains " + cacheSize + " entries");
     * }</pre>
     *
     * @return the number of key-value mappings in this cache
     */
    int size();

    /**
     * Removes all entries from this cache.
     * <p>
     * After this operation, the cache will be empty.
     * 
     * <pre>{@code
     * // Clear the entire cache
     * cache.invalidate();
     * System.out.println("Cache size after invalidation: " + cache.size()); // Should be 0
     * }</pre>
     *
     * @return this cache instance for method chaining
     */
    Cache<K, V> invalidate();

    /**
     * Removes the entry for the specified key from this cache if it exists.
     * <p>
     * If the key is not present in the cache, this method has no effect.
     * 
     * <pre>{@code
     * // Remove a specific user from the cache
     * cache.invalidate("john.doe");
     * 
     * // Verify the user is no longer in the cache
     * Optional<User> user = cache.get("john.doe");
     * System.out.println("User exists in cache: " + user.isPresent()); // Should be false
     * }</pre>
     *
     * @param k the key whose mapping is to be removed from the cache
     * @return this cache instance for method chaining
     */
    Cache<K, V> invalidate(@Nullable K k);

}
