package com.wiredi.runtime.domain.scopes;

import com.wiredi.runtime.domain.BeanFactory;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.scopes.cache.SingeltonScopeStore;
import com.wiredi.runtime.domain.scopes.exceptions.ScopeNotActivatedException;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalScope extends AbstractScope {

    private final ThreadLocal<SingeltonScopeStore> cache = new ThreadLocal<>();

    public ThreadLocalScope(Map<TypeIdentifier, BeanFactory> factories) {
        super(false, factories);
    }

    public ThreadLocalScope() {
        this(new HashMap<>());
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
    public boolean isActive() {
        return cache.get() != null;
    }

    @Override
    public void doStart() {
        SingeltonScopeStore store = this.cache.get();
        if (store == null) {
            this.cache.set(new SingeltonScopeStore());
        }
    }

    @Override
    public void doFinish() {
        SingeltonScopeStore delegate = getCache();
        delegate.tearDown();
        cache.remove();
    }

    @Override
    protected void checkActive() {
        if (this.cache.get() == null) {
            throw new ScopeNotActivatedException(this, "No thread bound ScopeStore found. Please start the scope before using it.");
        }
    }
}
