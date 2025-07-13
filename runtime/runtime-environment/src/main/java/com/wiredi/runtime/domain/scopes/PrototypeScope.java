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

    public PrototypeScope(boolean autostart, Map<TypeIdentifier, BeanFactory> factories, ScopeStore store) {
        super(autostart, factories);
        this.scopeStore = store;
    }

    public PrototypeScope(boolean autostart, Map<TypeIdentifier, BeanFactory> factories) {
        this(autostart, factories, new PrototypeStore());
    }

    public PrototypeScope(boolean autostart) {
        this(autostart, new HashMap<>(), new PrototypeStore());
    }

    public PrototypeScope() {
        this(true, new HashMap<>(), new PrototypeStore());
    }

    public static PrototypeScope threadSafe(boolean autostart) {
        return new PrototypeScope(autostart, new ConcurrentHashMap<>(), ScopeStore.threadSafe());
    }

    public static PrototypeScope threadSafe() {
        return new PrototypeScope(true, new ConcurrentHashMap<>(), ScopeStore.threadSafe());
    }

    @Override
    protected @NotNull ScopeStore scopeStore() {
        return scopeStore;
    }
}
