package com.wiredi.runtime.domain;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.lang.OrderedComparator;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class ScopeRegistry {

    /**
     * This map contains all registered Scopes.
     * <p>
     * The key right here is the key to identify the scope.
     * Most commonly, this is an annotation, which is meta annotated with {@link jakarta.inject.Scope}.
     */
    @NotNull
    private final Map<Object, Scope> scopes = new HashMap<>();
    /**
     * The cache for resolving type -> Scope associations.
     * <p>
     * If you have a class {@code Foo} and Foo is registered as a Prototype, this cache allows you to resolve the Scope for the TypeIdentifier.
     * <p>
     * The key is an object to support qualified Type references
     */
    @NotNull
    private final ScopeRegistryCache scopeRegistryCache = new ScopeRegistryCache();
    @NotNull
    private final Scope defaultScope;
    @Nullable
    private WireContainer context;

    public ScopeRegistry() {
        this(Singleton.class, Scope.threadSafeSingleton());
    }

    public ScopeRegistry(@NotNull Object identifier, @NotNull Scope defaultScope) {
        this.defaultScope = defaultScope;
        defaultScope.registered(this);
        scopes.put(identifier, defaultScope);
    }

    public void initialize(WireContainer wireContext) {
        if (this.context != null) {
            throw new IllegalStateException("ScopeRegistry has already been initialized");
        }
        this.context = wireContext;
        for (Scope value : scopes.values()) {
            value.link(wireContext);
        }
        for (Scope value : scopes.values()) {
            value.autostart();
        }
    }

    public void tearDown() {
        for (Scope value : scopes.values()) {
            value.finish();
        }
        for (Scope value : scopes.values()) {
            value.unregistered(this);
        }

        this.scopeRegistryCache.clear();
        this.scopes.clear();
        this.context = null;
    }

    @Nullable
    public Scope getScope(@Nullable Object key) {
        return scopes.get(key);
    }

    @NotNull
    public Scope getDefaultScope() {
        return defaultScope;
    }

    public boolean isInitialized() {
        return context != null;
    }

    /**
     * Registers a provider with the appropriate scope based on its scope() method.
     * This is the key optimization method that routes providers to the right scope.
     */
    public void registerProvider(@NotNull IdentifiableProvider<?> provider) {
        Scope targetScope = resolveTargetScopeForRegistration(provider);
        targetScope.register(provider);

        if (provider.primary()) {
            scopeRegistryCache.registerPrimaryProvider(provider);
        }

        if (targetScope != defaultScope) {
            // Use the ScopeRegistryCache to update the cache
            scopeRegistryCache.updateCacheForProvider(provider, targetScope);
        }
    }

    /**
     * Resolves the appropriate scope for a provider based on its scope() declaration.
     */
    @NotNull
    private Scope resolveTargetScopeForRegistration(@NotNull IdentifiableProvider<?> provider) {
        ScopeProvider scopeProvider = provider.scope();

        if (scopeProvider == null) {
            return defaultScope;
        }

        return Objects.requireNonNullElse(scopeProvider.getScope(this), defaultScope);
    }

    /**
     * Registers a new scope with the registry and ensures it's properly linked.
     */
    public void register(@NotNull Object key, @NotNull Scope scope) {
        if (this.scopes.containsKey(key) && this.scopes.get(key) == scope) {
            throw new IllegalArgumentException("Scope with key " + key + " is already registered!");
        }

        Scope registeredScope = this.scopes.computeIfAbsent(key, k -> {
            tryToLink(scope);
            return scope;
        });

        if (!registeredScope.equals(scope)) {
            throw new ConcurrentModificationException("Scope with key " + key + " is already registered! Attempted to register " + scope + " in place of " + registeredScope);
        }
    }

    @NotNull
    public Scope registerIfAbsent(@NotNull Object key, @NotNull Supplier<@NotNull Scope> scopeSupplier) {
        return Objects.requireNonNull(scopes.computeIfAbsent(key, k -> {
            Scope scope = scopeSupplier.get();
            tryToLink(scope);
            return scope;
        }), () -> "It is not allowed to register a null scope with key " + key + "!");
    }

    private void tryToLink(Scope scope) {
        scope.registered(this);
        if (context != null) {
            scope.link(context);
            scope.autostart();
        }
    }

    /**
     * Für getAll() - search all scopes
     */
    public <T> List<T> getAllInstances(TypeIdentifier<T> type) {
        return scopes.values().stream()
                .flatMap(it -> it.getAllBeans(type))
                .sorted(OrderedComparator.INSTANCE)
                .map(Bean::instance)
                .toList();
    }

    public <T> Scope determineScopeOf(TypeIdentifier<T> typeIdentifier) {
        return scopeRegistryCache.determineScopeOf(typeIdentifier, defaultScope);
    }

    public <T> Scope determineScopeOf(QualifiedTypeIdentifier<T> typeIdentifier) {
        return scopeRegistryCache.determineScopeOf(typeIdentifier, defaultScope);
    }
}
