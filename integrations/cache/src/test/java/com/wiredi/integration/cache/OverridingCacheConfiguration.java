package com.wiredi.integration.cache;

import com.wiredi.annotations.Order;
import com.wiredi.annotations.Provider;
import com.wiredi.annotations.Wire;
import com.wiredi.runtime.cache.CacheManager;
import com.wiredi.runtime.domain.conditional.builtin.ConditionalOnProperty;

@Wire(proxy = false)
@ConditionalOnProperty(key = "test", havingValue = "true")
@Order(before = CacheAutoConfiguration.class)
public class OverridingCacheConfiguration {

    @Provider
    public CacheManager cacheManager() {
        return new OverwrittenCacheManager();
    }
}
