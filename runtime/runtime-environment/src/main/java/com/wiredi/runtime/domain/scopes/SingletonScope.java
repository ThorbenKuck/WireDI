package com.wiredi.runtime.domain.scopes;

import com.wiredi.runtime.domain.BeanFactory;
import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.scopes.cache.ScopeStore;
import com.wiredi.runtime.domain.scopes.cache.SingeltonScopeStore;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class SingletonScope extends AbstractScope {

    private final ScopeStore scopeStore;
    public static final Supplier<Scope> SUPPLIER = SingletonScope::new;

    public SingletonScope(Map<TypeIdentifier, BeanFactory> factories, ScopeStore scopeStore) {
        super(factories);
        this.scopeStore = scopeStore;
    }

    public SingletonScope(Map<TypeIdentifier, BeanFactory> factories) {
        this(factories, new SingeltonScopeStore());
    }

    public SingletonScope() {
        this(new HashMap<>(), new SingeltonScopeStore());
    }

    public static SingletonScope threadSafe() {
        return new SingletonScope(new ConcurrentHashMap<>(), ScopeStore.threadSafe());
    }

    @Override
    protected @NotNull ScopeStore scopeStore() {
        return scopeStore;
    }
}
