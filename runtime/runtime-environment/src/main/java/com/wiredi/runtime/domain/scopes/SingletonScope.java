package com.wiredi.runtime.domain.scopes;

import com.wiredi.runtime.domain.BeanFactory;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.scopes.cache.ScopeStore;
import com.wiredi.runtime.domain.scopes.cache.SingeltonScopeStore;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonScope extends AbstractScope {

    private final ScopeStore scopeStore;

    public SingletonScope(boolean autostart, Map<TypeIdentifier, BeanFactory> factories, ScopeStore scopeStore) {
        super(autostart, factories);
        this.scopeStore = scopeStore;
    }

    public SingletonScope(boolean autostart, Map<TypeIdentifier, BeanFactory> factories) {
        this(autostart, factories, new SingeltonScopeStore());
    }

    public SingletonScope(boolean autostart) {
        this(autostart, new HashMap<>(), new SingeltonScopeStore());
    }

    public SingletonScope() {
        this(true);
    }

    public static SingletonScope threadSafe(boolean autostart) {
        return new SingletonScope(autostart, new ConcurrentHashMap<>(), ScopeStore.threadSafe());
    }

    public static SingletonScope threadSafe() {
        return new SingletonScope(true, new ConcurrentHashMap<>(), ScopeStore.threadSafe());
    }

    @Override
    protected @NotNull ScopeStore scopeStore() {
        return scopeStore;
    }
}
