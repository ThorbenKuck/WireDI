package com.wiredi.runtime.domain.scopes.cache;

import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class SingeltonScopeStore implements ScopeStore {

    // Thread-local for tracking loading state to prevent circular dependencies
    private static final ThreadLocal<Set<Object>> LOADING_TRACKER = ThreadLocal.withInitial(HashSet::new);
    private final Map<IdentifiableProvider<?>, Bean<?>> providerCache;
    private final Map<TypeIdentifier<?>, Collection<Bean<?>>> allInstances;

    public SingeltonScopeStore(
            Map<IdentifiableProvider<?>, Bean<?>> providerCache,
            Map<TypeIdentifier<?>, Collection<Bean<?>>> allInstances
    ) {
        this.providerCache = providerCache;
        this.allInstances = allInstances;
    }

    public SingeltonScopeStore() {
        this.providerCache = new HashMap<>();
        this.allInstances = new HashMap<>();
    }

    public static SingeltonScopeStore threadSafe() {
        return new SingeltonScopeStore(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
    }

    @Override
    public @Nullable <T> Bean<T> getOrSet(
            @NotNull IdentifiableProvider<T> provider,
            @NotNull TypeIdentifier<?> type,
            @NotNull Supplier<@Nullable Bean<T>> instanceFactory
    ) {
        // Check if already cached (fast path)
        Bean<T> existing = (Bean<T>) providerCache.get(provider);
        if (existing != null) {
            return existing;
        }

        // Check for circular dependency
        Set<Object> currentlyLoading = LOADING_TRACKER.get();
        if (currentlyLoading.contains(provider)) {
            return null; // Circular dependency detected, return null to break the cycle
        }

        // Use manual synchronization instead of computeIfAbsent to avoid recursive update
        currentlyLoading.add(provider);
        try {
            // Double-check after acquiring the lock
            existing = (Bean<T>) providerCache.get(provider);
            if (existing != null) {
                return existing;
            }

            Bean<T> newBean = instanceFactory.get();
            if (newBean != null) {
                providerCache.put(provider, newBean);
                allInstances.computeIfAbsent(type, t -> new ArrayList<>()).add(newBean);
            }
            return newBean;
        } finally {
            currentlyLoading.remove(provider);
        }
    }

    @Override
    public <T> @NotNull Optional<Bean<T>> getOrTrySet(
            @NotNull IdentifiableProvider<T> provider,
            @NotNull TypeIdentifier<?> type,
            @NotNull Supplier<@NotNull Optional<Bean<T>>> instance
    ) {
        // Check if already cached (fast path)
        Bean<T> existing = (Bean<T>) providerCache.get(provider);
        if (existing != null) {
            return Optional.of(existing);
        }

        // Check for circular dependency
        Set<Object> currentlyLoading = LOADING_TRACKER.get();
        if (currentlyLoading.contains(provider)) {
            return Optional.empty(); // Circular dependency detected, return empty to break the cycle
        }

        // Use manual synchronization instead of computeIfAbsent to avoid recursive update
        currentlyLoading.add(provider);
        try {
            // Double-check after acquiring the lock
            existing = (Bean<T>) providerCache.get(provider);
            if (existing != null) {
                return Optional.of(existing);
            }

            Optional<Bean<T>> newBeanOpt = instance.get();
            Bean<T> newBean = newBeanOpt.orElse(null);
            if (newBean != null) {
                providerCache.put(provider, newBean);
                allInstances.computeIfAbsent(type, t -> new ArrayList<>()).add(newBean);
            }
            return Optional.ofNullable(newBean);
        } finally {
            currentlyLoading.remove(provider);
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Collection<Bean<T>> getAll(TypeIdentifier<T> type, Supplier<Collection<Bean<T>>> supplier) {
        // Fast path: already cached
        Collection<Bean<T>> cached = (Collection) allInstances.get(type);
        if (cached != null) {
            return cached;
        }

        // Recursion check
        Set<Object> currentlyLoading = LOADING_TRACKER.get();
        if (currentlyLoading.contains(type)) {
            return Collections.emptyList();
        }

        // Use manual synchronization instead of computeIfAbsent to avoid recursive update
        currentlyLoading.add(type);
        try {
            // Double-check after acquiring the lock
            cached = (Collection) allInstances.get(type);
            if (cached != null) {
                return cached;
            }

            Collection<Bean<T>> newCollection = supplier.get();
            allInstances.put(type, (Collection) newCollection);
            return newCollection;
        } finally {
            currentlyLoading.remove(type);
        }
    }

    @Override
    public void tearDown() {
        providerCache.clear();
        allInstances.clear();
        LOADING_TRACKER.remove(); // Clean up thread-local
    }
}