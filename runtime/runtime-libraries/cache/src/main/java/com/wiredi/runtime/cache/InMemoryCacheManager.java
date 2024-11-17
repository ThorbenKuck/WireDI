package com.wiredi.runtime.cache;

import org.jetbrains.annotations.NotNull;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InMemoryCacheManager implements CacheManager {

    @NotNull
    private final Map<CacheIdentifier<?, ?>, Cache<?, ?>> caches = new HashMap<>();
    @NotNull
    private final Map<Object, Lock> locks = new HashMap<>();
    @NotNull
    private final InMemoryCacheConfiguration cacheConfiguration;

    public InMemoryCacheManager(@NotNull InMemoryCacheConfiguration cacheConfiguration) {
        this.cacheConfiguration = cacheConfiguration;
    }

    public InMemoryCacheManager() {
        this.cacheConfiguration = InMemoryCacheConfiguration.DEFAULT;
    }

    @Override
    public <K, V> @NotNull Cache<K, V> getCache(@NotNull CacheIdentifier<K, V> cacheIdentifier) {
        return getLocked(cacheIdentifier, () -> getOrCreateCache(cacheIdentifier));
    }

    public <K, V> void modifyCache(@NotNull CacheIdentifier<K, V> cacheIdentifier, @NotNull Consumer<Cache<K, V>> consumer) {
        runLocked(cacheIdentifier, () -> {
            Cache<K, V> cache = getOrCreateCache(cacheIdentifier);
            consumer.accept(cache);
        });
    }

    private <K, V> @NotNull Cache<K, V> getOrCreateCache(@NotNull CacheIdentifier<K, V> cacheIdentifier) {
        return (Cache<K, V>) caches.computeIfAbsent(cacheIdentifier, (n) -> new InMemoryCache<>(cacheConfiguration));
    }

    private void runLocked(@NotNull Object key, Runnable runnable) {
        Lock lock = getLock(key);
        try {
            lock.lock();
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    private <T> T getLocked(@NotNull Object key, Supplier<T> supplier) {
        Lock lock = getLock(key);
        try {
            lock.lock();
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    private Lock getLock(Object key) {
        if (!locks.containsKey(key)) {
            synchronized (locks) {
                if (!locks.containsKey(key)) {
                    locks.put(key, new ReentrantLock());
                }
            }
        }

        Lock lock = locks.get(key);
        if (lock == null) {
            throw new ConcurrentModificationException("A lock was removed from the lock contents");
        }
        return lock;
    }
}
