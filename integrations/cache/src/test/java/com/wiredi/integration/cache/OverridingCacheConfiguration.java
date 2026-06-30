package com.wiredi.integration.cache;

import com.wiredi.annotations.Primary;
import com.wiredi.annotations.Provider;
import com.wiredi.annotations.stereotypes.Configuration;
import com.wiredi.runtime.cache.CacheManager;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnProperty;

@Configuration
@ConditionalOnProperty(key = "test", havingValue = "true")
public class OverridingCacheConfiguration {

    @Provider
    @Primary
    public CacheManager cacheManager() {
        return new OverwrittenCacheManager();
    }
}
