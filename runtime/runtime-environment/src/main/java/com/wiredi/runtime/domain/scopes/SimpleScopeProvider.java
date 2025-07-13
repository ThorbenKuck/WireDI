package com.wiredi.runtime.domain.scopes;

import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.ScopeProvider;
import com.wiredi.runtime.domain.ScopeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class SimpleScopeProvider implements ScopeProvider {

    private final Object identifier;
    private final Supplier<Scope> scopeSupplier;

    public SimpleScopeProvider(Object identifier) {
        this(identifier, () -> new SingletonScope(false));
    }

    public SimpleScopeProvider(Object identifier, Supplier<Scope> scopeSupplier) {
        this.identifier = identifier;
        this.scopeSupplier = scopeSupplier;
    }

    @Override
    public @NotNull Scope getScope(@NotNull ScopeRegistry registry) {
        return registry.registerIfAbsent(identifier, scopeSupplier);
    }

    public static class Builder {

        @NotNull
        private final Object identifier;
        @NotNull
        private Supplier<@NotNull Scope> scopeSupplier = () -> new SingletonScope(false);

        public Builder(@NotNull Object identifier) {
            this.identifier = identifier;
        }

        @NotNull
        public Builder withScope(@NotNull Scope scope) {
            this.scopeSupplier = () -> scope;
            return this;
        }

        @NotNull
        public Builder withScope(@NotNull Supplier<@NotNull Scope> supplier) {
            this.scopeSupplier = supplier;
            return this;
        }

        public SimpleScopeProvider build() {
            return new SimpleScopeProvider(identifier, scopeSupplier);
        }
    }
}
