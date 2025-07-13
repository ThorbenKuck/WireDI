package com.wiredi.runtime.domain.scopes.provider;

import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.ScopeProvider;
import com.wiredi.runtime.domain.ScopeRegistry;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingletonScopeProvider implements ScopeProvider {
    @Override
    public @NotNull Scope getScope(@NotNull ScopeRegistry registry) {
        return registry.registerIfAbsent(Singleton.class, Scope::singleton);
    }
}
