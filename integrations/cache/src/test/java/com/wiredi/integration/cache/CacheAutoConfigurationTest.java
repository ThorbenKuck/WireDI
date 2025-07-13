package com.wiredi.integration.cache;

import com.wiredi.annotations.ActiveProfiles;
import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.WiredApplication;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.cache.CacheManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles({"test"})
class CacheAutoConfigurationTest {
    @Test
    public void testThatTheCacheManagerIsWired() {
        // Arrange
        WireContainer wireContainer = WiredApplication.start().wireContainer();

        // Act
        // Assert
        assertThat(wireContainer.environment().activeProfiles()).isEqualTo(List.of("test"));
        assertThat(wireContainer.contains(CacheManager.class)).isTrue();
    }

    @Test
    public void testThatTheCacheManagerCanBeWiredInADependency() {
        // Arrange
        WireContainer wireContainer = WiredApplication.start().wireContainer();

        // Act
        // Assert
        assertThat(wireContainer.contains(Dependency.class)).isTrue();
        assertThat(wireContainer.get(Dependency.class).cacheManager()).isNotNull();
    }


    @Test
    public void testThatACustomCacheManagerCanBeSupplied() {
        // Arrange
        WireContainer wireContainer = WireContainer.create();
        wireContainer.environment().properties().set(Key.just("test"), "true");
        wireContainer.load();

        // Act
        // Assert
        assertThat(wireContainer.get(CacheManager.class)).isNotNull().isInstanceOf(OverwrittenCacheManager.class);
    }
}