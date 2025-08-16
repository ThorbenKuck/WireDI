package com.wiredi.runtime.cache;

import org.jetbrains.annotations.NotNull;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A thread-safe implementation of the {@link CacheManager} interface that manages
 * in-memory caches.
 * <p>
 * This implementation provides thread-safe access to {@link InMemoryCache} instances
 * using a per-cache locking mechanism. Each cache is identified by a {@link CacheIdentifier}
 * and is created on demand when requested.
 * <p>
 * Features:
 * <ul>
 *   <li>Thread-safe access to caches</li>
 *   <li>Lazy creation of cache instances</li>
 *   <li>Configurable cache behavior through {@link InMemoryCacheConfiguration}</li>
 *   <li>Support for atomic cache modifications</li>
 * </ul>
 * <p>
 * Basic usage examples:
 * 
 * <pre>{@code
 * // Create a cache manager with default configuration
 * CacheManager cacheManager = new InMemoryCacheManager();
 * 
 * // Get or create a cache for users
 * Cache<String, User> userCache = cacheManager.getCache(
 *     String.class, User.class, "userCache"
 * );
 * 
 * // Store and retrieve values
 * userCache.put("john.doe", new User("john.doe", "John Doe"));
 * Optional<User> user = userCache.get("john.doe");
 * 
 * // Create a cache manager with custom configuration
 * InMemoryCacheConfiguration config = InMemoryCacheConfiguration.builder()
 *     .withCapacity(200)
 *     .withReorderOnHit(true)  // Enable LRU behavior
 *     .withHitOnOverride(true)
 *     .build();
 * CacheManager customCacheManager = new InMemoryCacheManager(config);
 * }</pre>
 * 
 * <p>
 * Thread-safe cache modifications:
 * 
 * <pre>{@code
 * // Perform atomic modifications to a cache
 * InMemoryCacheManager cacheManager = new InMemoryCacheManager();
 * CacheIdentifier<String, User> userCacheId = 
 *     new CacheIdentifier<>(String.class, User.class, "userCache");
 *     
 * // This operation is atomic and thread-safe
 * cacheManager.modifyCache(userCacheId, cache -> {
 *     // Multiple operations on the cache
 *     cache.put("user1", new User("user1", "User One"));
 *     cache.put("user2", new User("user2", "User Two"));
 *     cache.invalidate("oldUser");
 * });
 * }</pre>
 */
public class InMemoryCacheManager implements CacheManager {

    /**
     * Registry of caches managed by this cache manager.
     * Maps cache identifiers to their corresponding cache instances.
     */
    @NotNull
    private final Map<CacheIdentifier<?, ?>, Cache<?, ?>> caches = new HashMap<>();
    
    /**
     * Registry of locks used for thread-safe access to caches.
     * Each cache identifier has its own lock to allow concurrent access to different caches.
     */
    @NotNull
    private final Map<Object, Lock> locks = new HashMap<>();
    
    /**
     * Configuration used when creating new cache instances.
     * All caches created by this manager will use this configuration.
     */
    @NotNull
    private final InMemoryCacheConfiguration cacheConfiguration;

    /**
     * Creates a new cache manager with the specified configuration.
     * <p>
     * All caches created by this manager will use the provided configuration.
     * 
     * <pre>{@code
     * // Create a cache manager with custom configuration
     * InMemoryCacheConfiguration config = InMemoryCacheConfiguration.builder()
     *     .withCapacity(200)
     *     .withReorderOnHit(true)
     *     .withHitOnOverride(true)
     *     .build();
     * CacheManager cacheManager = new InMemoryCacheManager(config);
     * }</pre>
     *
     * @param cacheConfiguration the configuration to use for all caches created by this manager
     */
    public InMemoryCacheManager(@NotNull InMemoryCacheConfiguration cacheConfiguration) {
        this.cacheConfiguration = cacheConfiguration;
    }

    /**
     * Creates a new cache manager with the default configuration.
     * <p>
     * The default configuration has:
     * <ul>
     *   <li>hitOnOverride = true</li>
     *   <li>reorderOnHit = false</li>
     *   <li>capacity = 50</li>
     * </ul>
     * 
     * <pre>{@code
     * // Create a cache manager with default configuration
     * CacheManager cacheManager = new InMemoryCacheManager();
     * }</pre>
     */
    public InMemoryCacheManager() {
        this.cacheConfiguration = InMemoryCacheConfiguration.DEFAULT;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation provides thread-safe access to caches. If the requested
     * cache doesn't exist, it is created using the manager's configuration.
     * <p>
     * The method uses a per-cache locking mechanism to ensure thread safety,
     * allowing concurrent access to different caches.
     * 
     * <pre>{@code
     * // Get or create a cache for users
     * Cache<String, User> userCache = cacheManager.getCache(
     *     String.class, User.class, "userCache"
     * );
     * 
     * // Use the cache
     * userCache.put("john.doe", new User("john.doe", "John Doe"));
     * Optional<User> user = userCache.get("john.doe");
     * }</pre>
     *
     * @param cacheIdentifier the identifier for the cache
     * @param <K> the key type
     * @param <V> the value type
     * @return the cache related to the identifier, creating it if it doesn't exist
     */
    @Override
    public <K, V> @NotNull Cache<K, V> getCache(@NotNull CacheIdentifier<K, V> cacheIdentifier) {
        return getLocked(cacheIdentifier, () -> getOrCreateCache(cacheIdentifier));
    }

    /**
     * Performs atomic modifications to a cache in a thread-safe manner.
     * <p>
     * This method allows multiple operations on a cache to be performed atomically,
     * ensuring that no other threads can access the cache during the modification.
     * <p>
     * This is useful when you need to perform multiple operations on a cache
     * as a single atomic unit, such as adding multiple entries or performing
     * complex updates.
     * 
     * <pre>{@code
     * // Perform atomic modifications to a cache
     * cacheManager.modifyCache(
     *     new CacheIdentifier<>(String.class, User.class, "userCache"),
     *     cache -> {
     *         // Multiple operations on the cache
     *         cache.put("user1", new User("user1", "User One"));
     *         cache.put("user2", new User("user2", "User Two"));
     *         cache.invalidate("oldUser");
     *     }
     * );
     * }</pre>
     *
     * @param cacheIdentifier the identifier for the cache to modify
     * @param consumer a function that performs operations on the cache
     * @param <K> the key type
     * @param <V> the value type
     */
    public <K, V> void modifyCache(@NotNull CacheIdentifier<K, V> cacheIdentifier, @NotNull Consumer<Cache<K, V>> consumer) {
        runLocked(cacheIdentifier, () -> {
            Cache<K, V> cache = getOrCreateCache(cacheIdentifier);
            consumer.accept(cache);
        });
    }

    /**
     * Gets an existing cache or creates a new one if it doesn't exist.
     * <p>
     * This method is called by {@link #getCache(CacheIdentifier)} and
     * {@link #modifyCache(CacheIdentifier, Consumer)} to retrieve or create
     * a cache for the given identifier.
     *
     * @param cacheIdentifier the identifier for the cache
     * @param <K> the key type
     * @param <V> the value type
     * @return the existing cache or a new one if it doesn't exist
     */
    private <K, V> @NotNull Cache<K, V> getOrCreateCache(@NotNull CacheIdentifier<K, V> cacheIdentifier) {
        return (Cache<K, V>) caches.computeIfAbsent(cacheIdentifier, (n) -> new InMemoryCache<>(cacheConfiguration));
    }

    /**
     * Executes a runnable while holding a lock for the specified key.
     * <p>
     * This method ensures that the runnable is executed atomically with respect
     * to other operations on the same key.
     *
     * @param key the key to lock on
     * @param runnable the code to execute while holding the lock
     */
    private void runLocked(@NotNull Object key, Runnable runnable) {
        Lock lock = getLock(key);
        try {
            lock.lock();
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Executes a supplier while holding a lock for the specified key and returns its result.
     * <p>
     * This method ensures that the supplier is executed atomically with respect
     * to other operations on the same key.
     *
     * @param key the key to lock on
     * @param supplier the code to execute while holding the lock
     * @param <T> the type of the result
     * @return the result of the supplier
     */
    private <T> T getLocked(@NotNull Object key, Supplier<T> supplier) {
        Lock lock = getLock(key);
        try {
            lock.lock();
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets or creates a lock for the specified key.
     * <p>
     * This method ensures that each key has its own lock, allowing concurrent
     * access to different keys while serializing access to the same key.
     * <p>
     * The method uses double-checked locking to minimize synchronization overhead.
     *
     * @param key the key to get a lock for
     * @return the lock for the specified key
     * @throws ConcurrentModificationException if a lock was removed concurrently
     */
    private Lock getLock(Object key) {
        if (!locks.containsKey(key)) {
            synchronized (locks) {
                if (!locks.containsKey(key)) {
                    locks.put(key, new ReentrantLock());
                }
            }
        }

        Lock lock = locks.get(key);
        if (lock == null) {
            throw new ConcurrentModificationException("A lock was removed from the lock contents");
        }
        return lock;
    }
}
