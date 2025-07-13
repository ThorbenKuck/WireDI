package com.wiredi.runtime.domain.scopes.cache;

import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class PrototypeStore implements ScopeStore {

    private final Map<TypeIdentifier<?>, List<Bean<?>>> beans;

    public PrototypeStore(Map<TypeIdentifier<?>, List<Bean<?>>> beans) {
        this.beans = beans;
    }

    public PrototypeStore() {
        this(new HashMap<>());
    }

    @Override
    public <T> @Nullable Bean<T> getOrSet(
            @NotNull IdentifiableProvider<T> provider,
            @NotNull TypeIdentifier<?> type,
            @NotNull Supplier<@Nullable Bean<T>> instanceFactory
    ) {
        Bean<T> bean = instanceFactory.get();
        if (bean != null) {
            List<Bean<?>> beans = this.beans.computeIfAbsent(type, t -> new ArrayList<>());
            beans.add(bean);
        }
        return bean;
    }

    @Override
    public <T> @NotNull Optional<Bean<T>> getOrTrySet(
            @NotNull IdentifiableProvider<T> provider,
            @NotNull TypeIdentifier<?> type,
            @NotNull Supplier<@NotNull Optional<Bean<T>>> instanceFactory
    ) {
        Optional<Bean<T>> bean = instanceFactory.get();
        bean.ifPresent(b -> {
            List<Bean<?>> beans = this.beans.computeIfAbsent(type, t -> new ArrayList<>());
            beans.add(b);
        });

        return bean;
    }

    @Override
    public void tearDown() {
        beans.values().forEach(beanInstances -> beanInstances.forEach(Bean::tearDown));
    }

    @Override
    public <T> Collection<Bean<T>> getAll(TypeIdentifier<T> type, Supplier<Collection<Bean<T>>> supplier) {
        Collection<Bean<T>> beans = supplier.get();
        this.beans.computeIfAbsent(type, (t) -> new ArrayList<>()).addAll(beans);
        return beans;
    }
}
