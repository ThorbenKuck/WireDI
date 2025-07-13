package com.wiredi.runtime.domain;

import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.exceptions.MultiplePrimaryProvidersRegisteredException;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for storing and retrieving scope information for types.
 * This class encapsulates the logic for managing the scopedCache map and primary provider cache.
 */
public class ScopeRegistryCache {

    /**
     * The cache for resolving type -> Scope associations.
     * <p>
     * If you have a class {@code Foo} and Foo is registered as a Prototype, this cache allows you to resolve the Scope for the TypeIdentifier.
     * <p>
     * The key is an object to support qualified Type references
     */
    @NotNull
    private final Map<Object, Scope> scopedCache = new ConcurrentHashMap<>();

    /**
     * Cache for primary providers across all scopes.
     * Maps TypeIdentifier to the primary provider for that type.
     */
    @NotNull
    private final Map<TypeIdentifier<?>, IdentifiableProvider<?>> primaryProviderCache = new ConcurrentHashMap<>();

    private final Set<TypeIdentifier<?>> conflictingTypes = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static final Logger logger = LoggerFactory.getLogger(ScopeRegistryCache.class);

    /**
     * Clears the cache.
     */
    public void clear() {
        this.scopedCache.clear();
        this.primaryProviderCache.clear();
        this.conflictingTypes.clear();
    }

    /**
     * Registers a primary provider for a type and all its additional wire types.
     * This method ensures that primary providers are available for cross-scope resolution.
     *
     * @param provider    The primary provider to register
     * @throws MultiplePrimaryProvidersRegisteredException if a primary provider is already registered for the type
     */
    public void registerPrimaryProvider(@NotNull IdentifiableProvider<?> provider) {
        setPrimaryProviderScope(provider.type(), provider);

        // Register primary provider for all additional wire types
        for (TypeIdentifier<?> additionalType : provider.additionalWireTypes()) {
            setPrimaryProviderScope(additionalType, provider);
        }

        logger.debug("Registered primary provider for type {} and {} additional wire types", 
                     provider.type(), provider.additionalWireTypes().size());
    }

    private void setPrimaryProviderScope(@NotNull TypeIdentifier<?> type, IdentifiableProvider<?> provider) {
        TypeIdentifier<?> mainType = type.erasure();
        IdentifiableProvider<?> existingPrimary = primaryProviderCache.get(mainType);
        if (existingPrimary != null) {
            throw new MultiplePrimaryProvidersRegisteredException(mainType, existingPrimary, provider);
        }

        primaryProviderCache.put(mainType, provider);
    }

    /**
     * Gets the primary provider for a given type.
     *
     * @param type The type to look up
     * @return The primary provider for the type, or null if none exists
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> IdentifiableProvider<T> getPrimaryProvider(@NotNull TypeIdentifier<T> type) {
        TypeIdentifier<?> erasedType = type.erasure();
        return (IdentifiableProvider<T>) primaryProviderCache.get(erasedType);
    }

    /**
     * Checks if a primary provider exists for the given type.
     *
     * @param type The type to check
     * @return true if a primary provider exists, false otherwise
     */
    public boolean hasPrimaryProvider(@NotNull TypeIdentifier<?> type) {
        return primaryProviderCache.containsKey(type.erasure());
    }

    /**
     * Updates the cache when a provider is registered.
     *
     * @param provider    The provider being registered
     * @param targetScope The scope the provider is being registered with
     */
    public void updateCacheForProvider(
            @NotNull IdentifiableProvider<?> provider,
            @NotNull Scope targetScope
    ) {
        // Qualified caching (always safe)
        for (QualifierType qualifier : provider.qualifiers()) {
            QualifiedTypeIdentifier<?> qualifiedType = provider.type().qualified(qualifier);
            scopedCache.put(qualifiedType, targetScope);

            // Additional wire types (qualified)
            for (TypeIdentifier<?> wireType : provider.additionalWireTypes()) {
                scopedCache.put(wireType.qualified(qualifier), targetScope);
            }
        }

        TypeIdentifier<?> erasedType = provider.type().erasure();

        // Check if type is already registered in another, non-default scope
        if (scopedCache.containsKey(erasedType)) {
            // Conflict! Don't cache
            scopedCache.remove(erasedType);
            conflictingTypes.add(erasedType); // Blacklist for conflicts
            logger.warn("Found duplicated scope registration for " + erasedType + " in scope " + targetScope);
        } else if (!conflictingTypes.contains(erasedType)) {
            scopedCache.put(erasedType, targetScope);
        }

        for (TypeIdentifier<?> wireType : provider.additionalWireTypes()) {
            TypeIdentifier<?> erasedWireType = wireType.erasure();
            if (scopedCache.containsKey(erasedWireType)) {
                scopedCache.remove(erasedWireType);
                conflictingTypes.add(erasedWireType);
            } else if (!conflictingTypes.contains(erasedWireType)) {
                scopedCache.put(erasedWireType, targetScope);
            }
        }
    }

    /**
     * Gets the cached scope for a qualified type.
     *
     * @param qualifiedType The qualified type to look up
     * @return The cached scope, or null if not found
     */
    @Nullable
    public Scope getCachedScope(@NotNull QualifiedTypeIdentifier<?> qualifiedType) {
        return scopedCache.get(qualifiedType);
    }

    /**
     * Determines the scope of a type identifier.
     * If a primary provider exists for the type, the scope containing that provider is returned.
     * Otherwise, falls back to the cached scope or default scope.
     *
     * @param typeIdentifier The type identifier to look up
     * @param defaultScope   The default scope to return if not found
     * @return The scope for the type
     */
    public <T> Scope determineScopeOf(TypeIdentifier<T> typeIdentifier, @NotNull Scope defaultScope) {
        // First check if there's a primary provider for this type
        IdentifiableProvider<T> primaryProvider = getPrimaryProvider(typeIdentifier);
        if (primaryProvider != null) {
            // Find the scope containing this primary provider
            Scope primaryScope = findScopeContainingProvider(primaryProvider, defaultScope);
            if (primaryScope != null) {
                return primaryScope;
            }
        }
        
        // Fall back to cached scope resolution
        TypeIdentifier<?> erasedType = typeIdentifier.erasure();
        Scope cachedScope = scopedCache.get(erasedType);
        return cachedScope != null ? cachedScope : defaultScope;
    }

    /**
     * Determines the scope of a qualified type identifier.
     *
     * @param typeIdentifier The qualified type identifier to look up
     * @param defaultScope   The default scope to return if not found
     * @return The scope for the qualified type
     */
    public <T> Scope determineScopeOf(QualifiedTypeIdentifier<T> typeIdentifier, @NotNull Scope defaultScope) {
        // For qualified types, we first check the direct cache
        Scope cachedScope = scopedCache.get(typeIdentifier);
        if (cachedScope != null) {
            return cachedScope;
        }

        // Then check if there's a primary provider for the unqualified type
        IdentifiableProvider<T> primaryProvider = getPrimaryProvider(typeIdentifier.type());
        if (primaryProvider != null) {
            // Find the scope containing this primary provider
            Scope primaryScope = findScopeContainingProvider(primaryProvider, defaultScope);
            if (primaryScope != null) {
                return primaryScope;
            }
        }

        // Fall back to unqualified type resolution
        return determineScopeOf(typeIdentifier.type(), defaultScope);
    }

    /**
     * Finds the scope that contains the given provider.
     * This is a helper method for primary provider resolution.
     *
     * @param provider     The provider to find
     * @param defaultScope The default scope to check first
     * @return The scope containing the provider, or null if not found
     */
    @Nullable
    private Scope findScopeContainingProvider(@NotNull IdentifiableProvider<?> provider, @NotNull Scope defaultScope) {
        // Check default scope first (most common case)
        if (defaultScope.contains(provider.type())) {
            return defaultScope;
        }
        
        // Check other scopes in the cache
        for (Scope scope : scopedCache.values()) {
            if (scope.contains(provider.type())) {
                return scope;
            }
        }
        
        return null;
    }
}