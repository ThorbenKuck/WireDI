package com.wiredi.integration.cache;

import com.wiredi.annotations.Provider;
import com.wiredi.annotations.stereotypes.AutoConfiguration;
import com.wiredi.runtime.cache.Cache;
import com.wiredi.runtime.cache.CacheManager;
import com.wiredi.runtime.cache.InMemoryCacheConfiguration;
import com.wiredi.runtime.cache.InMemoryCacheManager;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnEnabled;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@AutoConfiguration
@ConditionalOnEnabled("wiredi.autoconfig.cache")
public class CacheAutoConfiguration {

    @Provider
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(@Nullable InMemoryCacheConfiguration cacheConfiguration) {
        return new InMemoryCacheManager(Optional.ofNullable(cacheConfiguration).orElse(InMemoryCacheConfiguration.DEFAULT));
    }

    @Provider
    public Cache cache(
            TypeIdentifier<Cache> typeIdentifier,
            CacheManager cacheManager
    ) {
        return cacheManager.getCache(
                typeIdentifier.getGenericTypes().get(0).getRootType(),
                typeIdentifier.getGenericTypes().get(1).getRootType(),
                "test"
        );
    }
}
