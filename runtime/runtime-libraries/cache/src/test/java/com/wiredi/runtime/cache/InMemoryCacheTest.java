package com.wiredi.runtime.cache;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryCacheTest {

    private InMemoryCache<String, String> createCache(CacheConfiguration cacheConfiguration) {
        InMemoryCache<String, String> cache = new InMemoryCache<>(cacheConfiguration);

        cache.put("1", "1");
        cache.put("2", "2");
        cache.put("3", "3");

        return cache;
    }

    @Test
    public void verifyThatAHitPreventsFromCapacityBasedEvict() {
        // Arrange
        InMemoryCache<String, String> cache = createCache(
                CacheConfiguration.newInstance()
                        .withCapacity(3)
                        .build()
        );
        cache.get("1"); // Hit entry 1
        assertContainsInOrder(cache, "1", "2", "3");

        // Act
        // Will overflow the cache
        cache.put("4", "4");

        // Assert
        assertContainsInOrder(cache, "1", "3", "4");
    }

    @Test
    public void verifyThatAHitPreventsFromCapacityBasedEvictWithTwoHits() {
        // Arrange
        InMemoryCache<String, String> cache = createCache(
                CacheConfiguration.newInstance()
                        .withCapacity(3)
                        .build()
        );
        cache.get("1"); // Hit entry 1
        cache.get("2"); // Hit entry 2

        // Act
        // Will overflow the cache
        assertContainsInOrder(cache, "1", "2", "3");
        cache.put("4", "4");

        // Assert
        assertContainsInOrder(cache, "2", "3", "4");
    }

    @Test
    public void verifyThatReorderOnHitTwiceChangesTheEvictionTarget() {
        // Arrange
        InMemoryCache<String, String> cache = createCache(
                CacheConfiguration.newInstance()
                        .withReorderOnHit(true)
                        .withHitOnOverride(false)
                        .withCapacity(3)
                        .build()
        );
        cache.get("1"); // Hit entry 1
        cache.get("2"); // Hit entry 2
        assertContainsInOrder(cache, "3", "1", "2");

        // Act
        // Will overflow the cache
        // We expect 3 to be the first, as 1 and then 2 have been moved to the back
        cache.put("4", "4");

        // Assert
        assertContainsInOrder(cache, "1", "2", "4");
    }

    @Test
    public void verifyThatReorderOnHitChangesTheEvictionTarget() {
        // Arrange
        InMemoryCache<String, String> cache = createCache(
                CacheConfiguration.newInstance()
                        .withReorderOnHit(true)
                        .withHitOnOverride(false)
                        .withCapacity(3)
                        .build()
        );
        cache.get("1"); // Hit entry 1
        assertContainsInOrder(cache, "2", "3", "1");

        // Act
        // Will overflow the cache
        // We expect 3 to be the first, as 1 and then 2 have been moved to the back
        cache.put("4", "4");

        // Assert
        assertContainsInOrder(cache, "3", "1", "4");
    }

    @Test
    public void verifyThatFirstLowestHitIsInvalidated() {
        // Arrange
        InMemoryCache<String, String> cache = createCache(
                CacheConfiguration.newInstance()
                        .withReorderOnHit(true)
                        .withHitOnOverride(false)
                        .withCapacity(3)
                        .build()
        );
        cache.get("1"); // Hit entry 1
        cache.get("1"); // Hit entry 1
        cache.get("1"); // Hit entry 1
        cache.get("2"); // Hit entry 2
        cache.get("2"); // Hit entry 2
        cache.get("3"); // Hit entry 3
        assertContainsInOrder(cache, "1", "2", "3");

        // Act
        // Will overflow the cache
        // We expect 3 to be the one to go, as 1 and then 2 have been hit more times
        cache.put("4", "4");

        // Assert
        assertContainsInOrder(cache, "1", "2", "4");

        // Act
        // Now we will hit two and 4 more times than 1
        cache.get("4");
        cache.get("4");
        cache.get("4");
        cache.get("4");
        cache.get("2");
        cache.get("2");
        cache.get("2");
        // Will overflow the cache
        // Insert 5, which should evict 1
        cache.put("5", "5");

        // Assert
        assertContainsInOrder(cache, "4", "2", "5");
    }

    private void assertContainsInOrder(InMemoryCache<String, String> cache, String... keys) {
        InMemoryCache.Node<String, String> node = cache.first;
        List<String> result = new ArrayList<>();
        while (node != null) {
            result.add(node.value);
            node = node.next;
        }

        assertThat(result).containsExactlyElementsOf(Arrays.asList(keys));
    }
}