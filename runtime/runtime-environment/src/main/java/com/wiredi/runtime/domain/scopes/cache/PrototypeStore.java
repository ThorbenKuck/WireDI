package com.wiredi.runtime.domain.scopes.cache;

import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class PrototypeStore implements ScopeStore {

    private final Map<TypeIdentifier<?>, List<Bean<?>>> beans = new HashMap<>();

    @Override
    public <T> @Nullable Bean<T> getOrSet(@NotNull QualifiedTypeIdentifier<T> type, @NotNull Function<@NotNull QualifiedTypeIdentifier<T>, @Nullable Bean<T>> instance) {
        Bean<T> bean = instance.apply(type);
        if (bean != null) {
            List<Bean<?>> beans = this.beans.computeIfAbsent(type.type(), t -> new ArrayList<>());
            beans.add(bean);
        }
        return bean;
    }

    @Override
    public <T> @NotNull Optional<Bean<T>> getOrTrySet(@NotNull QualifiedTypeIdentifier<T> type, @NotNull Function<QualifiedTypeIdentifier<T>, Optional<Bean<T>>> instance) {
        Optional<Bean<T>> bean = instance.apply(type);
        bean.ifPresent(b -> {
            List<Bean<?>> beans = this.beans.computeIfAbsent(type.type(), t -> new ArrayList<>());
            beans.add(b);
        });

        return bean;
    }

    @Override
    public void tearDown() {
        beans.values().forEach(beanInstances -> beanInstances.forEach(Bean::tearDown));
    }

    @Override
    public <T> Collection<T> getAll(TypeIdentifier<T> type) {
        return List.of();
    }
}
