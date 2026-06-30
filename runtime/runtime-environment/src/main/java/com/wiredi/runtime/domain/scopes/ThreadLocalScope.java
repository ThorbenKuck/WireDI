package com.wiredi.runtime.domain.scopes;

import com.wiredi.runtime.domain.BeanFactory;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.scopes.cache.SingeltonScopeStore;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalScope extends AbstractScope {

    private final ThreadLocal<SingeltonScopeStore> cache = new ThreadLocal<>();
    private boolean createStoreOnDemand = false;

    public ThreadLocalScope(Map<TypeIdentifier, BeanFactory> factories) {
        super(factories);
    }

    public ThreadLocalScope() {
        this(new HashMap<>());
    }

    @Override
    protected @NotNull SingeltonScopeStore scopeStore() {
        return getCachedValue();
    }

    @Override
    public void start() {
        SingeltonScopeStore store = this.cache.get();

        if (store != null) {
            this.cache.set(new SingeltonScopeStore());
        }
    }

    @Override
    public void doReset() {
        SingeltonScopeStore delegate = getCachedValue();
        delegate.tearDown();
        cache.remove();
    }

    private SingeltonScopeStore getCachedValue() {
        SingeltonScopeStore store = this.cache.get();

        if (store == null) {
            if (!createStoreOnDemand) {
                throw new IllegalStateException("Scope not started. No thread bound ScopeStore found.");
            } else {
                store = new SingeltonScopeStore();
                this.cache.set(store);
            }
        }

        return store;
    }
}
