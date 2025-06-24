package com.wiredi.runtime.domain.scopes.cache;

import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class SingeltonScopeStore implements ScopeStore {

    private final Map<QualifiedTypeIdentifier<?>, Bean> concreteInstance;
    private final Map<TypeIdentifier<?>, Collection<Bean>> allInstances;

    public SingeltonScopeStore(Map<QualifiedTypeIdentifier<?>, Bean> concreteInstance, Map<TypeIdentifier<?>, Collection<Bean>> allInstances) {
        this.concreteInstance = concreteInstance;
        this.allInstances = allInstances;
    }

    public SingeltonScopeStore() {
        this.concreteInstance = new HashMap<>();
        this.allInstances = new HashMap<>();
    }

    @Override
    public @Nullable <T> Bean<T> getOrSet(@NotNull QualifiedTypeIdentifier<T> type, @NotNull Function<QualifiedTypeIdentifier<T>, @Nullable Bean<T>> instance) {
        return (Bean<T>) concreteInstance.computeIfAbsent(type, t -> instance.apply(type));
    }

    @Override
    public <T> @NotNull Optional<Bean<T>> getOrTrySet(@NotNull QualifiedTypeIdentifier<T> type, @NotNull Function<QualifiedTypeIdentifier<T>, Optional<Bean<T>>> instance) {
        Bean<T> bean = concreteInstance.computeIfAbsent(type, t -> instance.apply(type).orElse(null));
        return Optional.ofNullable(bean);
    }

    @Override
    public void tearDown() {
        Set<Bean> instances = new HashSet<>(concreteInstance.values());
        instances.addAll(allInstances.values().stream().flatMap(Collection::stream).toList());

        allInstances.clear();
        concreteInstance.clear();

        instances.forEach(Bean::tearDown);
    }

    @Override
    public <T> Collection<T> getAll(TypeIdentifier<T> type) {
        return (Collection<T>) Objects.requireNonNullElse(allInstances.get(type), Collections.emptyList());
    }
}
