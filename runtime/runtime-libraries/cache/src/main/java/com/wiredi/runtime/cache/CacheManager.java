package com.wiredi.runtime.cache;

import org.jetbrains.annotations.NotNull;

/**
 * An abstraction to manage and access caches within an application.
 * <p>
 * The CacheManager serves as a central registry for caches, allowing applications
 * to create, retrieve, and manage multiple cache instances. It acts as a factory
 * for {@link Cache} instances, creating them on demand when they don't exist.
 * <p>
 * Implementations of this interface may provide different caching strategies,
 * persistence options, or distribution mechanisms. The default implementation
 * provided by the framework is {@link InMemoryCacheManager}, which manages
 * in-memory caches with configurable eviction policies.
 * <p>
 * Basic usage examples:
 * 
 * <pre>{@code
 * // Create a cache manager
 * CacheManager cacheManager = new InMemoryCacheManager();
 * 
 * // Get or create a cache for users
 * Cache<String, User> userCache = cacheManager.getCache(
 *     String.class, User.class, "userCache"
 * );
 * 
 * // Store and retrieve data from the cache
 * User user = new User("john.doe", "John Doe");
 * userCache.put(user.getUsername(), user);
 * Optional<User> cachedUser = userCache.get("john.doe");
 * 
 * // Get or create another cache for different data
 * Cache<Integer, Product> productCache = cacheManager.getCache(
 *     Integer.class, Product.class, "productCache"
 * );
 * }</pre>
 * 
 * <p>
 * In a dependency injection context, you can typically inject a CacheManager:
 * 
 * <pre>{@code
 * @Wire
 * public class UserService {
 *     private final Cache<String, User> userCache;
 *     
 *     @Inject
 *     public UserService(CacheManager cacheManager) {
 *         this.userCache = cacheManager.getCache(String.class, User.class, "userCache");
 *     }
 *     
 *     // Use the cache in service methods
 * }
 * }</pre>
 */
public interface CacheManager {
    /**
     * Returns the cache identified by the {@code cacheIdentifier}.
     * <p>
     * This is the primary method for obtaining a cache from the CacheManager.
     * If the {@link Cache} does not exist for this {@link CacheIdentifier},
     * this CacheManager will create it using its default configuration.
     * <p>
     * The CacheIdentifier contains both type information (key and value types)
     * and a unique identifier, allowing the CacheManager to maintain type safety
     * while providing multiple caches for different purposes.
     * 
     * <pre>{@code
     * // Create a cache identifier for a user cache
     * CacheIdentifier<String, User> userCacheId = 
     *     new CacheIdentifier<>(String.class, User.class, "userCache");
     *     
     * // Get or create the cache using the identifier
     * Cache<String, User> userCache = cacheManager.getCache(userCacheId);
     * }</pre>
     *
     * @param cacheIdentifier the identifier for the cache, containing key type, value type, and a unique name
     * @param <K> the key type for the cache
     * @param <V> the value type for the cache
     * @return the {@link Cache} related to the {@link CacheIdentifier}, creating it if it doesn't exist
     */
    <K, V> @NotNull Cache<K, V> getCache(@NotNull CacheIdentifier<K, V> cacheIdentifier);

    /**
     * Returns the cache identified by the combination of key type, value type, and identifier.
     * <p>
     * This is a convenience method that creates a {@link CacheIdentifier} from the provided
     * parameters and delegates to {@link #getCache(CacheIdentifier)}.
     * <p>
     * If the {@link Cache} does not exist for this combination of types and identifier,
     * this CacheManager will create it using its default configuration.
     * 
     * <pre>{@code
     * // Get or create a cache for users
     * Cache<String, User> userCache = cacheManager.getCache(
     *     String.class, User.class, "userCache"
     * );
     * 
     * // Get or create a cache for products
     * Cache<Integer, Product> productCache = cacheManager.getCache(
     *     Integer.class, Product.class, "productCache"
     * );
     * }</pre>
     *
     * @param keyType the class object representing the type of keys in the cache
     * @param valueType the class object representing the type of values in the cache
     * @param cacheIdentifier a unique identifier for the cache (e.g., a string name)
     * @param <K> the key type for the cache
     * @param <V> the value type for the cache
     * @return the {@link Cache} related to the specified types and identifier, creating it if it doesn't exist
     */
    default <K, V> @NotNull Cache<K, V> getCache(Class<K> keyType, Class<V> valueType, @NotNull Object cacheIdentifier) {
        return getCache(new CacheIdentifier<>(keyType, valueType, cacheIdentifier));
    }

    /**
     * Returns a cache with Object keys and values, identified by the provided identifier.
     * <p>
     * This is a convenience method that creates a {@link CacheIdentifier} with
     * {@code Object.class} for both key and value types, and delegates to 
     * {@link #getCache(CacheIdentifier)}.
     * <p>
     * Since this method uses {@code Object} for both key and value types, it provides
     * the least type safety. It's recommended to use {@link #getCache(Class, Class, Object)}
     * instead when the key and value types are known.
     * 
     * <pre>{@code
     * // Get or create a generic cache with Object keys and values
     * Cache<Object, Object> genericCache = cacheManager.getCache("genericCache");
     * 
     * // Store any type of data (with type casting required when retrieving)
     * genericCache.put("user1", new User("john.doe", "John Doe"));
     * genericCache.put(42, new Product(42, "Widget"));
     * 
     * // Retrieving requires casting
     * Optional<Object> obj = genericCache.get("user1");
     * if (obj.isPresent()) {
     *     User user = (User) obj.get(); // Cast required
     *     System.out.println(user.getName());
     * }
     * }</pre>
     *
     * @param cacheIdentifier a unique identifier for the cache (e.g., a string name)
     * @return a {@link Cache} with Object keys and values, creating it if it doesn't exist
     */
    default @NotNull Cache<Object, Object> getCache(@NotNull Object cacheIdentifier) {
        return getCache(new CacheIdentifier<>(Object.class, Object.class, cacheIdentifier));
    }
}
