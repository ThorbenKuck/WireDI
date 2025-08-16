package com.wiredi.runtime.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * An in-memory implementation of the {@link Cache} interface.
 * <p>
 * This implementation stores cache entries in memory using a combination of a HashMap
 * for fast lookups and a doubly-linked list for maintaining order and implementing
 * eviction policies. The cache behavior is controlled by an {@link InMemoryCacheConfiguration}.
 * <p>
 * Features:
 * <ul>
 *   <li>Configurable capacity with automatic eviction when capacity is reached</li>
 *   <li>Support for both Least Recently Used (LRU) and Least Frequently Used (LFU) eviction policies</li>
 *   <li>Configurable hit counting behavior</li>
 * </ul>
 * <p>
 * Basic usage examples:
 * 
 * <pre>{@code
 * // Create a cache with default configuration
 * Cache<String, User> userCache = new InMemoryCache<>();
 * 
 * // Create a cache with custom capacity
 * Cache<Integer, Product> productCache = new InMemoryCache<>(100);
 * 
 * // Create a cache with custom configuration
 * InMemoryCacheConfiguration config = InMemoryCacheConfiguration.builder()
 *     .withCapacity(200)
 *     .withReorderOnHit(true)  // Enable LRU behavior
 *     .withHitOnOverride(true)
 *     .build();
 * Cache<String, Document> documentCache = new InMemoryCache<>(config);
 * 
 * // Store and retrieve values
 * userCache.put("john.doe", new User("john.doe", "John Doe"));
 * Optional<User> user = userCache.get("john.doe");
 * 
 * // Get with default value supplier
 * User user = userCache.getOr("jane.doe", () -> {
 *     // This will only be called if the user is not in the cache
 *     return fetchUserFromDatabase("jane.doe");
 * });
 * }</pre>
 * 
 * <p>
 * Note: While this class can be used directly, it's recommended to obtain cache
 * instances from a {@link CacheManager} to ensure proper configuration and
 * thread safety:
 * 
 * <pre>{@code
 * CacheManager cacheManager = new InMemoryCacheManager();
 * Cache<String, User> userCache = cacheManager.getCache(
 *     String.class, User.class, "userCache"
 * );
 * }</pre>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of values maintained by this cache
 */
public class InMemoryCache<K, V> implements Cache<K, V>{

    /** 
     * Map that stores cache entries for fast key-based lookups.
     * The keys are the cache keys, and the values are Node objects containing the cached values.
     */
    private final @NotNull Map<@Nullable K, @NotNull Node<K, V>> map;
    
    /** Configuration that controls the behavior of this cache. */
    private final @NotNull InMemoryCacheConfiguration configuration;
    
    /** 
     * Reference to the first node in the doubly-linked list.
     * This is typically the least valuable node (first to be evicted).
     */
    @VisibleForTesting
    @Nullable
    Node<K, V> first;
    
    /** 
     * Reference to the last node in the doubly-linked list.
     * This is typically the most valuable node (last to be evicted).
     */
    @VisibleForTesting
    @Nullable
    Node<K, V> last;
    
    /** The current number of entries in the cache. */
    private int size;

    /**
     * Creates a new in-memory cache with the specified capacity.
     * <p>
     * This constructor creates a cache with default settings except for the capacity.
     * 
     * <pre>{@code
     * // Create a cache that can hold up to 100 entries
     * Cache<String, User> userCache = new InMemoryCache<>(100);
     * }</pre>
     *
     * @param capacity the maximum number of entries the cache can hold
     */
    public InMemoryCache(int capacity) {
        this(InMemoryCacheConfiguration.builder().withCapacity(capacity).build());
    }

    /**
     * Creates a new in-memory cache with the default configuration.
     * <p>
     * The default configuration has:
     * <ul>
     *   <li>hitOnOverride = true</li>
     *   <li>reorderOnHit = false</li>
     *   <li>capacity = 50</li>
     * </ul>
     * 
     * <pre>{@code
     * // Create a cache with default settings
     * Cache<String, User> userCache = new InMemoryCache<>();
     * }</pre>
     */
    public InMemoryCache() {
        this(InMemoryCacheConfiguration.DEFAULT);
    }

    /**
     * Creates a new in-memory cache with the specified configuration.
     * <p>
     * This constructor allows full control over the cache's behavior through
     * a custom configuration.
     * 
     * <pre>{@code
     * // Create a cache with custom configuration
     * InMemoryCacheConfiguration config = InMemoryCacheConfiguration.builder()
     *     .withCapacity(200)
     *     .withReorderOnHit(true)  // Enable LRU behavior
     *     .withHitOnOverride(true)
     *     .build();
     * Cache<String, Document> documentCache = new InMemoryCache<>(config);
     * }</pre>
     *
     * @param configuration the configuration that controls the cache's behavior
     */
    public InMemoryCache(@NotNull InMemoryCacheConfiguration configuration) {
        this.configuration = configuration;
        this.map = new HashMap<>(configuration.capacity(), 1.1f);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation adds or updates a value in the cache. If the key already exists,
     * the value is updated. If the key doesn't exist, a new entry is created. If the cache
     * is at capacity, an entry will be evicted according to the cache's eviction policy.
     * <p>
     * The behavior when overriding an existing entry is controlled by the
     * {@code hitOnOverride} configuration option. If true, overriding an entry
     * counts as a hit, which can affect eviction decisions.
     * 
     * <pre>{@code
     * // Add a new entry to the cache
     * cache.put("key1", "value1");
     * 
     * // Update an existing entry
     * cache.put("key1", "updatedValue");
     * }</pre>
     *
     * @param key the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key (must not be null)
     * @return this cache instance for method chaining
     */
    @Override
    public Cache<K, V> put(@Nullable K key, @NotNull V value) {
        Node<K, V> existing = map.get(key);
        if(existing == null) {
            Node<K, V> node = new Node<>(key, value);
            if(size() >= configuration.capacity()) {
                invalidateNext();
            }
            addNodeToLast(node);
            map.put(key, node);
        } else {
            existing.value = value;
            if (configuration.hitOnOverride()) {
                hit(existing);
            }
        }

        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation retrieves a value from the cache by its key. If the key
     * is found, the associated entry's hit count is incremented, and the entry
     * may be reordered based on the {@code reorderOnHit} configuration option.
     * <p>
     * If the key is not found, an empty Optional is returned.
     * 
     * <pre>{@code
     * // Retrieve a value from the cache
     * Optional<String> value = cache.get("key1");
     * if (value.isPresent()) {
     *     System.out.println("Found value: " + value.get());
     * } else {
     *     System.out.println("Key not found in cache");
     * }
     * }</pre>
     *
     * @param key the key whose associated value is to be returned
     * @return an Optional containing the value to which the specified key is mapped,
     *         or an empty Optional if the cache contains no mapping for the key
     */
    @Override
    public @NotNull Optional<V> get(@Nullable K key) {
        return Optional.ofNullable(map.get(key)).map(node -> {
            hit(node);
            return node.value;
        });
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns the current number of entries in the cache.
     * 
     * <pre>{@code
     * // Get the current size of the cache
     * int cacheSize = cache.size();
     * System.out.println("Cache contains " + cacheSize + " entries");
     * }</pre>
     *
     * @return the number of key-value mappings in this cache
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation removes all entries from the cache, clearing both
     * the internal map and the doubly-linked list.
     * 
     * <pre>{@code
     * // Clear the entire cache
     * cache.invalidate();
     * System.out.println("Cache size after invalidation: " + cache.size()); // Should be 0
     * }</pre>
     *
     * @return this cache instance for method chaining
     */
    @Override
    public Cache<K, V> invalidate() {
        map.clear();
        @Nullable Node<K, V> pointer = first;
        first = null;
        last = null;
        while (pointer != null) {
            pointer = pointer.invalidateAndGetNext();
        }

        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation removes the entry for the specified key from the cache
     * if it exists, removing it from both the internal map and the doubly-linked list.
     * <p>
     * If the key is not present in the cache, this method has no effect.
     * 
     * <pre>{@code
     * // Remove a specific entry from the cache
     * cache.invalidate("key1");
     * 
     * // Verify the entry is no longer in the cache
     * Optional<String> value = cache.get("key1");
     * System.out.println("Entry exists in cache: " + value.isPresent()); // Should be false
     * }</pre>
     *
     * @param k the key whose mapping is to be removed from the cache
     * @return this cache instance for method chaining
     */
    @Override
    public Cache<K, V> invalidate(@Nullable K k) {
        @Nullable Node<K, V> node = map.get(k);
        if (node != null) {
            invalidate(node);
        }
        return this;
    }

    /**
     * Records a hit on a cache entry and potentially reorders it.
     * <p>
     * This method is called when a cache entry is accessed or updated.
     * It increments the hit count of the node and, if configured to do so,
     * reorders the node in the linked list to implement LRU behavior.
     *
     * @param node the cache entry that was hit
     */
    private void hit(@NotNull Node<K, V> node) {
        node.incrementHitCount();
        if (configuration.reorderOnHit()) {
            reorder(node);
        }
    }

    /**
     * Invalidates the next entry to be evicted when the cache is at capacity.
     * <p>
     * This method implements the cache's eviction policy. It selects the entry
     * with the lowest hit count (or the first entry if hit counts are equal)
     * and removes it from the cache.
     */
    private void invalidateNext() {
        if (first == null) {
            return;
        }
        if(first == last) {
            invalidate(first);
            return;
        }

        Node<K, V> invalidationTarget = first;
        while(invalidationTarget.next != null && invalidationTarget.next.hitCount < invalidationTarget.hitCount) {
            invalidationTarget = invalidationTarget.next;
        }
        invalidate(invalidationTarget);
    }

    /**
     * Reorders a node by moving it to the end of the linked list.
     * <p>
     * This method implements a Least Recently Used (LRU) policy by moving
     * the accessed node to the end of the list, making it the last to be
     * evicted. Nodes at the beginning of the list are evicted first.
     *
     * @param node the node to reorder
     */
    private void reorder(@NotNull Node<K, V> node) {
        if(last == node) {
            return;
        }
        if(first == node) {
            first = node.next;
            first.prev = null;
        } else {
            node.prev.next = node.next;
            node.next.prev = node.prev;

        }
        last.next = node;
        node.prev = last;
        last = node;
        node.next = null;
    }

    /**
     * Reorders a node based on its hit count (Least Frequently Used policy).
     * <p>
     * This method implements a Least Frequently Used (LFU) policy by moving
     * the node to a position in the list based on its hit count. Nodes with
     * higher hit counts are moved toward the end of the list, making them
     * less likely to be evicted.
     *
     * @param node the node to reorder
     */
    private void reorderWithLFU(@NotNull Node<K, V> node) {
        if(last == node) {
            return;
        }
        @Nullable Node<K, V> nextNode = node.next;
        while (nextNode != null) {
            if(nextNode.hitCount > node.hitCount) {
                break;
            }
            if(first == node) {
                first = nextNode;
            }
            if(node.prev != null) {
                node.prev.next = nextNode;
            }
            nextNode.prev = node.prev;
            node.prev = nextNode;
            node.next = nextNode.next;
            if(nextNode.next != null) {
                nextNode.next.prev = node;
            }
            nextNode.next = node;
            nextNode = node.next;
        }
        if(node.next == null) {
            last = node;
        }
    }

    /**
     * Adds a new node to the end of the linked list.
     * <p>
     * This method adds a node to the end of the doubly-linked list,
     * making it the last to be evicted. If the list is empty, the node
     * becomes both the first and last node.
     *
     * @param node the node to add to the list
     */
    private void addNodeToLast(@NotNull Node<K, V> node) {
        if(last != null) {
            last.next = node;
            node.prev = last;
        }

        last = node;
        if(first == null) {
            first = node;
        }
        size++;
    }

    /**
     * Removes a node from the linked list and the cache map.
     * <p>
     * This method handles the removal of a node from the doubly-linked list,
     * updating the first and last references as needed, and removes the
     * corresponding entry from the map.
     *
     * @param node the node to remove from the cache
     */
    private void invalidate(@NotNull Node<K, V> node) {
        if(last == node) {
            last = node.prev;
            last.next = null;
        } else if(first == node) {
            first = node.next;
            first.prev = null;
        } else {
            node.next.prev = node.prev;
            node.prev.next = node.next;
        }
        map.remove(node.key);
        size--;
        node.prev = null;
        node.next = null;
    }

    /**
     * A node in the cache's doubly-linked list that represents a single cache entry.
     * <p>
     * Each node contains a key-value pair and maintains references to the previous
     * and next nodes in the list. It also tracks the number of times the entry has
     * been accessed (hit count), which is used for eviction decisions.
     * <p>
     * The doubly-linked list structure allows for efficient reordering and removal
     * of entries, which is essential for implementing various cache eviction policies.
     *
     * @param <K> the type of keys maintained by this cache node
     * @param <V> the type of values maintained by this cache node
     */
    @VisibleForTesting
    static final class Node<K, V> {
        /**
         * The key associated with this cache entry.
         * This is final and cannot be changed after the node is created.
         */
        @Nullable
        @VisibleForTesting
        final K key;

        /**
         * The value associated with this cache entry.
         * This can be updated when the cache entry is overridden.
         */
        @Nullable
        @VisibleForTesting
        V value;

        /**
         * The number of times this cache entry has been accessed.
         * This is used for implementing Least Frequently Used (LFU) eviction policy.
         */
        private long hitCount = 0;

        /**
         * References to the previous and next nodes in the doubly-linked list.
         * These are used for efficient traversal, reordering, and removal of nodes.
         */
        @Nullable
        @VisibleForTesting
        Node<K, V> prev, next;

        /**
         * Creates a new cache node with the specified key and value.
         *
         * @param key the key associated with this cache entry
         * @param value the value associated with this cache entry
         */
        public Node(@Nullable K key, @Nullable V value) {
            this.value = value;
            this.key = key;
        }

        /**
         * Invalidates this node by clearing its references and returns the next node.
         * <p>
         * This method is used during cache invalidation to efficiently traverse
         * and clear the linked list.
         *
         * @return the next node in the list, or null if there is no next node
         */
        public @Nullable Node<K, V> invalidateAndGetNext() {
            Node<K, V> returnValue = next;
            prev = null;
            next = null;
            return returnValue;
        }

        /**
         * Increments the hit count for this cache entry.
         * <p>
         * The hit count is used for implementing Least Frequently Used (LFU)
         * eviction policy. Entries with higher hit counts are considered more
         * valuable and are less likely to be evicted.
         * <p>
         * The hit count is capped at Long.MAX_VALUE to prevent overflow.
         */
        public void incrementHitCount() {
            if (this.hitCount != Long.MAX_VALUE) {
                this.hitCount++;
            }
        }

        /**
         * Returns a string representation of this cache entry.
         *
         * @return a string representation of this cache entry
         */
        @Override
        public String toString() {
            return "CacheItem(" + key + "," + value + ")";
        }
    }
}
