package com.wiredi.runtime.domain.scopes.provider;

import com.wiredi.annotations.scopes.Prototype;
import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.ScopeProvider;
import com.wiredi.runtime.domain.ScopeRegistry;
import com.wiredi.runtime.domain.scopes.PrototypeScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrototypeScopeProvider implements ScopeProvider {
    @Override
    public @NotNull Scope getScope(@NotNull ScopeRegistry registry) {
        return registry.registerIfAbsent(Prototype.class, PrototypeScope::new);
    }
}
