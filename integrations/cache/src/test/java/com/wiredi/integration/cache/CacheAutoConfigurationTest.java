package com.wiredi.integration.cache;

import com.wiredi.annotations.ActiveProfiles;
import com.wiredi.runtime.WiredApplication;
import com.wiredi.runtime.WiredApplicationInstance;
import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.cache.CacheManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles({"test", "example"})
class CacheAutoConfigurationTest {
    @Test
    public void testThatTheCacheManagerIsWired() {
        // Arrange
        WireRepository wireRepository = WiredApplication.start().repository();

        // Act
        // Assert
        assertThat(wireRepository.environment().activeProfiles()).isEqualTo(List.of("test", "example"));
        assertThat(wireRepository.contains(CacheManager.class)).isTrue();
    }

    @Test
    public void testThatTheCacheManagerCanBeWiredInADependency() {
        // Arrange
        WireRepository wireRepository = WiredApplication.start().repository();

        // Act
        // Assert
        assertThat(wireRepository.contains(Dependency.class)).isTrue();
        assertThat(wireRepository.get(Dependency.class).cacheManager()).isNotNull();
    }


    @Test
    public void testThatACustomCacheManagerCanBeSupplied() {
        // Arrange
        WireRepository wireRepository = WireRepository.create();
        wireRepository.environment().properties().set(Key.just("test"), "true");
        wireRepository.load();

        // Act
        // Assert
        assertThat(wireRepository.get(CacheManager.class)).isNotNull().isInstanceOf(OverwrittenCacheManager.class);
    }
}