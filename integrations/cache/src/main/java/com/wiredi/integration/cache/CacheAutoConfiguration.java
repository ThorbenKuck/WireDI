package com.wiredi.integration.cache;

import com.wiredi.annotations.Provider;
import com.wiredi.annotations.stereotypes.AutoConfiguration;
import com.wiredi.runtime.cache.InMemoryCacheConfiguration;
import com.wiredi.runtime.cache.CacheManager;
import com.wiredi.runtime.cache.InMemoryCacheManager;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnMissingBean;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@AutoConfiguration
@ConditionalOnMissingBean(type = CacheManager.class)
@ConditionalOnProperty(
        key = "wiredi.cache.autoconfigure",
        havingValue = "true",
        matchIfMissing = true
)
public class CacheAutoConfiguration {

    @Provider
    public CacheManager cacheManager(@Nullable InMemoryCacheConfiguration cacheConfiguration) {
        return new InMemoryCacheManager(Optional.ofNullable(cacheConfiguration).orElse(InMemoryCacheConfiguration.DEFAULT));
    }
}
