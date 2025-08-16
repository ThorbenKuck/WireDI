package com.wiredi.runtime.cache;

/**
 * A record that uniquely identifies a cache within a {@link CacheManager}.
 * <p>
 * CacheIdentifier combines type information (key and value types) with a unique
 * identifier to allow the CacheManager to maintain multiple caches with different
 * purposes and type parameters. This approach ensures type safety while providing
 * flexibility in cache management.
 * <p>
 * The CacheIdentifier is used as a key in the CacheManager's internal registry,
 * allowing it to retrieve the appropriate cache instance when requested.
 * <p>
 * Basic usage examples:
 * 
 * <pre>{@code
 * // Create a cache identifier for a user cache
 * CacheIdentifier<String, User> userCacheId = 
 *     new CacheIdentifier<>(String.class, User.class, "userCache");
 *     
 * // Create a cache identifier for a product cache
 * CacheIdentifier<Integer, Product> productCacheId = 
 *     new CacheIdentifier<>(Integer.class, Product.class, "productCache");
 *     
 * // Get caches using these identifiers
 * CacheManager cacheManager = new InMemoryCacheManager();
 * Cache<String, User> userCache = cacheManager.getCache(userCacheId);
 * Cache<Integer, Product> productCache = cacheManager.getCache(productCacheId);
 * }</pre>
 * 
 * <p>
 * You can use any object as the identifier, but it's recommended to use simple,
 * immutable objects like Strings or Enums to avoid unexpected behavior:
 * 
 * <pre>{@code
 * // Using an enum as identifier
 * enum CacheType { USERS, PRODUCTS, ORDERS }
 * 
 * CacheIdentifier<String, User> userCacheId = 
 *     new CacheIdentifier<>(String.class, User.class, CacheType.USERS);
 * }</pre>
 * 
 * <p>
 * Note that the CacheManager also provides convenience methods that create
 * CacheIdentifier instances internally:
 * 
 * <pre>{@code
 * // This creates a CacheIdentifier internally
 * Cache<String, User> userCache = cacheManager.getCache(
 *     String.class, User.class, "userCache"
 * );
 * }</pre>
 *
 * @param <K> the type of keys maintained by the identified cache
 * @param <V> the type of values maintained by the identified cache
 */
public record CacheIdentifier<K, V>(
        /**
         * The Class object representing the type of keys in the cache.
         * This is used for type safety and to distinguish between caches
         * with the same identifier but different key types.
         */
        Class<K> keyType,
        
        /**
         * The Class object representing the type of values in the cache.
         * This is used for type safety and to distinguish between caches
         * with the same identifier but different value types.
         */
        Class<V> valueType,
        
        /**
         * A unique identifier for the cache. This can be any object,
         * but it's recommended to use simple, immutable objects like
         * Strings or Enums. The identifier's equals() and hashCode()
         * methods are used to identify the cache in the CacheManager.
         */
        Object identifier
) {
}
