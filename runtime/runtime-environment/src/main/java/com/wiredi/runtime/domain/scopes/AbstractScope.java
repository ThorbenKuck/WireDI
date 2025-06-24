package com.wiredi.runtime.domain.scopes;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.domain.BeanFactory;
import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.factories.MissingBeanException;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.scopes.cache.ScopeStore;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractScope implements Scope {

    @NotNull
    private final Map<@NotNull TypeIdentifier, @NotNull BeanFactory> factories;
    @Nullable
    private WireRepository wireRepository;

    public AbstractScope(@NotNull Map<@NotNull TypeIdentifier, @NotNull BeanFactory> factories) {
        this.factories = factories;
    }

    public AbstractScope() {
        this(new HashMap<>());
    }

    @NotNull
    protected abstract ScopeStore scopeStore();

    @Override
    public void register(@NotNull IdentifiableProvider<?> provider) {
        this.factories.computeIfAbsent(provider.type(), BeanFactory::of).register(provider);
        provider.additionalWireTypes().forEach(it -> this.factories.computeIfAbsent(it, BeanFactory::of).register(provider));
    }

    @Override
    public boolean contains(@NotNull QualifiedTypeIdentifier<?> type) {
        return factories.containsKey(type.type());
    }

    @Override
    public <T> @NotNull T get(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        Bean<T> bean = scopeStore().getOrSet(qualifierType, this::createNewInstance);

        if (bean == null) {
            throw MissingBeanException.unableToCreate(qualifierType.type());
        }

        return bean.instance();
    }

    @Override
    public <T> @NotNull Optional<T> tryGet(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        return scopeStore().getOrTrySet(qualifierType, this::tryCreateNewInstance)
                .map(Bean::instance);
    }

    private <T> @NotNull Bean<T> createNewInstance(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        BeanFactory<T> beanFactory = factories.get(qualifierType.type());
        if (beanFactory == null) {
            throw MissingBeanException.missingFactory(qualifierType.type());
        }

        QualifierType qualifier = qualifierType.qualifier();
        Bean<T> bean;
        if (qualifier != null) {
            bean = beanFactory.get(wireRepository, qualifierType.type(), qualifier);
        } else {
            bean = beanFactory.get(wireRepository, qualifierType.type());
        }

        if (bean == null) {
            throw MissingBeanException.unableToCreate(qualifierType.type());
        }

        return bean;
    }

    private <T> @Nullable Optional<Bean<T>> tryCreateNewInstance(QualifiedTypeIdentifier<T> qualifierType) {
        BeanFactory beanFactory = factories.get(qualifierType.type());
        if (beanFactory == null) {
            return Optional.empty();
        }

        QualifierType qualifier = qualifierType.qualifier();
        if (qualifier != null) {
            return Optional.ofNullable(beanFactory.get(wireRepository, qualifierType.type(), qualifier));
        } else {
            return Optional.ofNullable(beanFactory.get(wireRepository, qualifierType.type()));
        }
    }

    @Override
    public <T> @NotNull Collection<T> getAll(@NotNull TypeIdentifier<T> type) {
        return scopeStore().getAll(type);
    }

    @Override
    public boolean canSupply(@NotNull QualifiedTypeIdentifier<?> type) {
        return factories.containsKey(type.type());
    }

    @Override
    public void finish() {
        this.scopeStore().tearDown();
    }

    @Override
    public void link(@NotNull WireRepository wireRepository) {
        this.wireRepository = wireRepository;
    }
}
