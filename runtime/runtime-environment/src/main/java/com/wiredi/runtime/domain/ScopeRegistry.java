package com.wiredi.runtime.domain;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.scopes.CompositeScope;
import com.wiredi.runtime.domain.scopes.SingletonScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ScopeRegistry {

    private final WireRepository wireRepository;
    private final Map<Object, Scope> scopes = new HashMap<>();
    @NotNull
    private final Scope defaultScope;

    public ScopeRegistry(@NotNull Scope defaultScope, WireRepository wireRepository) {
        this.defaultScope = defaultScope;
        this.wireRepository = wireRepository;
    }

    public ScopeRegistry(WireRepository wireRepository) {
        this.wireRepository = wireRepository;
        this.defaultScope = new SingletonScope();
    }

    @NotNull
    public Scope getDefaultScope() {
        return defaultScope;
    }

    @Nullable
    public Scope getScope(@Nullable Object key) {
        return scopes.get(key);
    }

    /**
     * Registers a new {@code scope} associated with a {@code key}.
     * <p>
     * This method returns any scope that was previously associated with the {@code key}
     *
     * @param key   the key of this scope
     * @param scope the scope to register
     * @return the scope that was previously associated with the {@code key}
     */
    @Nullable
    public Scope register(@NotNull Object key, @NotNull Scope scope) {
        Scope oldValue = this.scopes.put(key, Scope.composite(scope, defaultScope));
        scope.link(wireRepository);
        if (oldValue != null) {
            oldValue.unregistered(this);
        }
        scope.registered(this);
        return oldValue;
    }

    /**
     * Registers a new {@code scopeSupplier} associated with a {@code key}.
     * <p>
     * This method returns any scopeSupplier that was previously associated with the {@code key}
     *
     * @param key   the key of this scopeSupplier
     * @param scopeSupplier the scopeSupplier to register
     * @return the scopeSupplier that was previously associated with the {@code key}
     */
    @Nullable
    public Scope registerIfAbsent(@NotNull Object key, @NotNull Supplier<@NotNull Scope> scopeSupplier) {
        return this.scopes.computeIfAbsent(key, k -> {
            Scope scope = scopeSupplier.get();
            scope.link(wireRepository);
            scope.registered(this);
            return Scope.composite(scope, defaultScope);
        });
    }
}
