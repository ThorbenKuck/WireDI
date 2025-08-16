package com.wiredi.runtime.cache;

/**
 * Configuration options for {@link InMemoryCache} instances.
 * <p>
 * This record provides configuration parameters that control the behavior of in-memory caches,
 * including eviction policies, hit counting, and capacity limits.
 * <p>
 * Basic usage examples:
 * 
 * <pre>{@code
 * // Using the builder pattern
 * InMemoryCacheConfiguration config = InMemoryCacheConfiguration.builder()
 *     .withCapacity(100)
 *     .withHitOnOverride(true)
 *     .withReorderOnHit(true)
 *     .build();
 *     
 * // Create a cache manager with this configuration
 * CacheManager cacheManager = new InMemoryCacheManager(config);
 * 
 * // Or use the default configuration
 * CacheManager defaultCacheManager = new InMemoryCacheManager(
 *     InMemoryCacheConfiguration.DEFAULT
 * );
 * }</pre>
 */
public record InMemoryCacheConfiguration(
        /**
         * Determines if a cache hit should be counted when an existing entry is overridden.
         * <p>
         * When set to true, calling {@code put()} on an existing key will increment
         * the hit count for that entry, which can affect cache eviction decisions.
         * When false, overriding an entry does not count as a hit.
         */
        boolean hitOnOverride,
        
        /**
         * Determines if cache entries should be reordered when they are accessed.
         * <p>
         * When set to true, accessing an entry (via {@code get()}) will move it
         * to a more favorable position in the cache's internal ordering, making it
         * less likely to be evicted. This implements a Least Recently Used (LRU)
         * eviction policy.
         * <p>
         * When false, the cache ordering is not affected by access patterns.
         */
        boolean reorderOnHit,
        
        /**
         * The maximum number of entries the cache can hold.
         * <p>
         * When the cache reaches this capacity and a new entry is added,
         * an existing entry will be evicted according to the cache's eviction policy.
         */
        int capacity
) {

    /**
     * A default configuration for in-memory caches.
     * <p>
     * This configuration has the following settings:
     * <ul>
     *   <li>hitOnOverride = true (overriding an entry counts as a hit)</li>
     *   <li>reorderOnHit = false (entries are not reordered when accessed)</li>
     *   <li>capacity = 50 (maximum of 50 entries in the cache)</li>
     * </ul>
     * <p>
     * Usage example:
     * <pre>{@code
     * // Create a cache manager with the default configuration
     * CacheManager cacheManager = new InMemoryCacheManager(
     *     InMemoryCacheConfiguration.DEFAULT
     * );
     * }</pre>
     */
    public static InMemoryCacheConfiguration DEFAULT = new InMemoryCacheConfiguration(true, false, 50);

    /**
     * Creates a new builder for constructing an {@link InMemoryCacheConfiguration}.
     * <p>
     * The builder provides a fluent API for setting configuration options.
     * <p>
     * Usage example:
     * <pre>{@code
     * InMemoryCacheConfiguration config = InMemoryCacheConfiguration.builder()
     *     .withCapacity(100)
     *     .withHitOnOverride(true)
     *     .withReorderOnHit(true)
     *     .build();
     * }</pre>
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder class for creating {@link InMemoryCacheConfiguration} instances.
     * <p>
     * This builder provides a fluent API for setting configuration options.
     * The default values are:
     * <ul>
     *   <li>hitOnOverride = false</li>
     *   <li>reorderOnHit = false</li>
     *   <li>capacity = 50</li>
     * </ul>
     */
    public static class Builder {
        /** Whether overriding an entry counts as a hit (default: false) */
        boolean hitOnOverride = false;
        
        /** Whether entries are reordered when accessed (default: false) */
        boolean reorderOnHit = false;
        
        /** Maximum number of entries in the cache (default: 50) */
        int capacity = 50;

        /**
         * Sets whether overriding an entry counts as a hit.
         *
         * @param hitOnOverride true if overriding an entry should count as a hit, false otherwise
         * @return this builder instance for method chaining
         */
        public Builder withHitOnOverride(boolean hitOnOverride) {
            this.hitOnOverride = hitOnOverride;
            return this;
        }

        /**
         * Sets whether entries should be reordered when accessed.
         * <p>
         * When true, this implements a Least Recently Used (LRU) eviction policy.
         *
         * @param reorderOnHit true if entries should be reordered when accessed, false otherwise
         * @return this builder instance for method chaining
         */
        public Builder withReorderOnHit(boolean reorderOnHit) {
            this.reorderOnHit = reorderOnHit;
            return this;
        }

        /**
         * Sets the maximum number of entries the cache can hold.
         *
         * @param capacity the maximum number of entries (should be greater than 0)
         * @return this builder instance for method chaining
         */
        public Builder withCapacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        /**
         * Builds a new {@link InMemoryCacheConfiguration} with the current settings.
         *
         * @return a new configuration instance
         */
        public InMemoryCacheConfiguration build() {
            return new InMemoryCacheConfiguration(hitOnOverride, reorderOnHit, capacity);
        }
    }
}
