package com.wiredi.runtime.domain.scopes.cache;

import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.exceptions.CyclicDependencyException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "rawtypes"})
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
    public @NotNull <T> Bean<T> getOrSet(
            @NotNull IdentifiableProvider<T> provider,
            @NotNull TypeIdentifier<?> type,
            @NotNull Supplier<@NotNull Bean<T>> beanSupplier
    ) {
        // Check if already cached (fast path)
        Bean<T> existing = (Bean<T>) providerCache.get(provider);
        if (existing != null) {
            return existing;
        }

        return runCycleFree(provider, () -> {
            // After adding the current provider, recheck the bean from the provider cache.
            // Theoretically, this could be because another thread has already created the bean before we added it to currently loading.
            Bean<T> cached = (Bean<T>) providerCache.get(provider);
            if (cached != null) {
                return cached;
            }

            Bean<T> newBean = beanSupplier.get();
            providerCache.put(provider, newBean);
            allInstances.computeIfAbsent(type, t -> new ArrayList<>()).add(newBean);
            return newBean;
        });
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

        return runCycleFree(provider, () -> {
            // Double-check after acquiring the lock
            Bean<T> cachedInstance = (Bean<T>) providerCache.get(provider);
            if (cachedInstance != null) {
                return Optional.of(cachedInstance);
            }

            Optional<Bean<T>> bean = instance.get();
            bean.ifPresent(b -> {
                providerCache.put(provider, b);
                allInstances.computeIfAbsent(type, t -> new ArrayList<>()).add(b);
            });
            return bean;
        });
    }

    @Override
    public <T> Collection<Bean<T>> getAll(TypeIdentifier<T> type, Supplier<@NotNull Collection<Bean<T>>> supplier) {
        // Fast path: already cached
        Collection<Bean<T>> cached = (Collection) allInstances.get(type);
        if (cached != null) {
            return cached;
        }

        return runCycleFree(type, () -> {
            // Double-check after acquiring the lock
            Collection<Bean<T>> cachedBeans = (Collection) allInstances.get(type);
            if (cachedBeans != null) {
                return cachedBeans;
            }

            Collection<Bean<T>> newCollection = supplier.get();
            allInstances.put(type, (Collection) newCollection);
            return newCollection;
        });
    }

    @Override
    public boolean contains(QualifiedTypeIdentifier<?> type) {
        return allInstances.containsKey(type.type());
    }

    @Override
    public boolean contains(TypeIdentifier<?> type) {
        return allInstances.containsKey(type);
    }

    /**
     * Tries to detect circular dependencies and run the supplier in a cycle-free manner.
     * <p>
     * A cycle can happen if a provider instantiates a bean that requires another bean which is currently being instantiated.
     * Contrary to cycles in a dependency graph, these cycles are due to condition evaluation and not due to circular references.
     * We break the cycle by returning an empty Optional instead of throwing an exception because the later process will correctly continue with this.
     *
     * @param lock     The lock object to synchronize on.
     * @param supplier The supplier function to execute.
     * @param <T>      The type of the result.
     * @return The result of the supplier function wrapped in an Optional, or empty if a circular dependency is detected.
     */
    @NotNull
    private <T> T runCycleFree(@NotNull Object lock, @NotNull Supplier<T> supplier) {
        Set<Object> currentlyLoading = LOADING_TRACKER.get();

        // Check for circular dependency
        if (currentlyLoading.contains(lock)) {
            // Intra-Thread cycle detected. Raise an exception, as a circular dependency is not allowed.
            throw new CyclicDependencyException(currentlyLoading);
        }
        // Avoid the use of computeIfAbsent to avoid recursive update resulting in dead locks
        // Use manual "synchronization" by using the thread-local currently loading instances
        currentlyLoading.add(lock);

        try {
            // Synchronize to prevent inter-thread deadlocks
            synchronized (lock) {
                return supplier.get();
            }
        } finally {
            currentlyLoading.remove(lock);
        }
    }

    @Override
    public void tearDown() {
        providerCache.values().forEach(Bean::tearDown);
        providerCache.clear();
        allInstances.clear();
        LOADING_TRACKER.remove(); // Clean up thread-local
    }
}