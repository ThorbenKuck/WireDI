package com.wiredi.integration.cache;

import com.wiredi.annotations.Wire;
import com.wiredi.runtime.cache.CacheManager;

@Wire
record Dependency(CacheManager cacheManager) {
}
