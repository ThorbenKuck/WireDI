package com.wiredi.runtime.domain.scopes;

import com.wiredi.runtime.domain.BeanFactory;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.scopes.cache.PrototypeStore;
import com.wiredi.runtime.domain.scopes.cache.ScopeStore;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PrototypeScope extends AbstractScope {

    private final ScopeStore store = new PrototypeStore();

    public PrototypeScope(Map<TypeIdentifier, BeanFactory> factories) {
        super(factories);
    }

    public PrototypeScope() {
    }

    @Override
    protected @NotNull ScopeStore scopeStore() {
        return store;
    }
}
