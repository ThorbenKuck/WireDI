package com.wiredi.runtime.domain.scopes;

import com.wiredi.runtime.domain.BeanFactory;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.scopes.cache.ScopeStore;
import com.wiredi.runtime.domain.scopes.cache.SingeltonScopeStore;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SingletonScope extends AbstractScope {

    private final ScopeStore scopeStore = new SingeltonScopeStore();

    public SingletonScope(Map<TypeIdentifier, BeanFactory> factories) {
        super(factories);
    }

    public SingletonScope() {
    }

    @Override
    protected @NotNull ScopeStore scopeStore() {
        return scopeStore;
    }
}
