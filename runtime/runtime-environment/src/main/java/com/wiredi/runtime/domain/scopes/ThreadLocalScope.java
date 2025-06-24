package com.wiredi.runtime.domain.scopes;

import com.wiredi.runtime.domain.BeanFactory;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.scopes.cache.SingeltonScopeStore;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ThreadLocalScope extends AbstractScope {

    private final ThreadLocal<SingeltonScopeStore> cache = new ThreadLocal<>();

    public ThreadLocalScope(Map<TypeIdentifier, BeanFactory> factories) {
        super(factories);
    }

    public ThreadLocalScope() {
    }

    @Override
    protected @NotNull SingeltonScopeStore scopeStore() {
        SingeltonScopeStore cache = this.cache.get();

        if (cache == null) {
            cache = new SingeltonScopeStore();
            this.cache.set(cache);
        }

        return cache;
    }

    private SingeltonScopeStore getCache() {
        SingeltonScopeStore cache = this.cache.get();

        if (cache == null) {
            throw new IllegalStateException("Scope not started. No thread bound ScopeStore found.");
        }

        return cache;
    }

    @Override
    public void start() {
        this.cache.set(new SingeltonScopeStore());
    }

    @Override
    public void finish() {
        SingeltonScopeStore delegate = getCache();
        delegate.tearDown();
        cache.remove();
    }
}
