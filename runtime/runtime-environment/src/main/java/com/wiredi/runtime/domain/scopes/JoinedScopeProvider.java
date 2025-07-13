package com.wiredi.runtime.domain.scopes;

import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.ScopeProvider;
import com.wiredi.runtime.domain.ScopeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class JoinedScopeProvider implements ScopeProvider {

    @NotNull
    private final List<@NotNull ScopeProvider> providers = new ArrayList<>();

    public JoinedScopeProvider(@NotNull List<ScopeProvider> providers) {
        this.providers.addAll(providers);
    }

    public JoinedScopeProvider(@NotNull ScopeProvider... providers) {
        this.providers.addAll(Arrays.asList(providers));
    }

    @Override
    public @NotNull Scope getScope(@NotNull ScopeRegistry registry) {
        return Collections.unmodifiableList(providers)
                .stream()
                .map(provider -> provider.getScope(registry))
                .filter(Objects::nonNull)
                .collect(CompositeScope.collector());
    }

    @Override
    public ScopeProvider and(ScopeProvider provider) {
        providers.add(provider);
        return this;
    }
}
