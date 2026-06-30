package com.wiredi.runtime.domain.scopes;

import com.wiredi.runtime.domain.BeanFactory;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.scopes.cache.PrototypeStore;
import com.wiredi.runtime.domain.scopes.cache.ScopeStore;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PrototypeScope extends AbstractScope {

    private final ScopeStore scopeStore;

    public PrototypeScope(Map<TypeIdentifier, BeanFactory> factories, ScopeStore store) {
        super(factories);
        this.scopeStore = store;
    }

    public PrototypeScope(Map<TypeIdentifier, BeanFactory> factories) {
        this(factories, new PrototypeStore());
    }

    public PrototypeScope() {
        this(new HashMap<>(), new PrototypeStore());
    }

    public static PrototypeScope threadSafe() {
        return new PrototypeScope(new ConcurrentHashMap<>(), ScopeStore.threadSafe());
    }

    @Override
    protected @NotNull ScopeStore scopeStore() {
        return scopeStore;
    }
}
