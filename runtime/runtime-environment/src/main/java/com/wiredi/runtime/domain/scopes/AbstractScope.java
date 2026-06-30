package com.wiredi.runtime.domain.scopes;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.BeanFactory;
import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.ScopeCallback;
import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.exceptions.MissingBeanException;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.scopes.cache.ScopeStore;
import com.wiredi.runtime.lang.OrderedComparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractScope implements Scope {

    @NotNull
    private final Map<@NotNull TypeIdentifier, @NotNull BeanFactory> factories;
    @Nullable
    private WireContainer wireContainer;
    @NotNull
    private ScopeCallback scopeCallback = ScopeCallback.NOOP;

    public AbstractScope(@NotNull Map<@NotNull TypeIdentifier, @NotNull BeanFactory> factories) {
        this.factories = factories;
    }

    @NotNull
    protected abstract ScopeStore scopeStore();

    @Override
    public void callback(@NotNull ScopeCallback scopeCallback) {
        this.scopeCallback = scopeCallback;
    }

    @Override
    public @NotNull <T> IdentifiableProvider<T> getProvider(@NotNull TypeIdentifier<T> typeIdentifier) {
        BeanFactory<T> factory = factories.get(typeIdentifier.erasure());

        if (factory == null) {
            throw MissingBeanException.missingFactory(typeIdentifier);
        }

        IdentifiableProvider<T> provider = factory.resolveProvider(null);
        if (provider != null) {
            return provider;
        }

        throw MissingBeanException.unableToCreate(typeIdentifier.erasure());
    }

    @Override
    public @NotNull <T> IdentifiableProvider<T> getProvider(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        BeanFactory<T> factory = factories.get(qualifierType.type().erasure());

        if (factory == null) {
            throw MissingBeanException.missingFactory(qualifierType.type());
        }

        IdentifiableProvider<T> provider = factory.resolveProvider(qualifierType.qualifier());
        if (provider != null) {
            return provider;
        }

        throw MissingBeanException.unableToCreate(qualifierType.type());
    }

    @Override
    public void register(@NotNull IdentifiableProvider<?> provider) {
        // Compute erasure once
        TypeIdentifier<?> erasedType = provider.type().erasure();

        // Direct computeIfAbsent with factory creation
        this.factories.computeIfAbsent(erasedType, BeanFactory::of).register(provider);

        // Batch register additional wire types
        for (TypeIdentifier<?> wireType : provider.additionalWireTypes()) {
            this.factories.computeIfAbsent(wireType.erasure(), BeanFactory::of).register(provider);
        }
    }

    @Override
    public @NotNull <T> T get(@NotNull TypeIdentifier<T> typeIdentifier) {
        // Use cached erasure for scope store key too
        TypeIdentifier<T> erased = typeIdentifier.erasure();
        // Direct factory lookup using cached erasure
        BeanFactory<T> factory = (BeanFactory<T>) factories.get(erased);
        if (factory == null) {
            throw MissingBeanException.unableToCreate(typeIdentifier);
        }

        IdentifiableProvider<T> provider = factory.resolveProvider(null);
        if (provider == null) {
            throw MissingBeanException.unableToCreate(typeIdentifier);
        }

        return scopeStore().getOrSet(provider, erased, () -> createNewBean(factory, typeIdentifier)).instance();
    }

    @Override
    public <T> @NotNull T get(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        TypeIdentifier<T> rootType = qualifierType.type();

        // Always lookup by erasure, but pass original qualified type to factory
        BeanFactory<T> factory = factories.get(rootType.erasure());
        if (factory == null) {
            throw MissingBeanException.unableToCreate(rootType);
        }

        IdentifiableProvider<T> provider = factory.resolveProvider(qualifierType.qualifier());
        if (provider == null) {
            throw MissingBeanException.unableToCreate(rootType);
        }

        return scopeStore().getOrSet(provider, rootType, () -> createNewBean(factory, qualifierType))
                .instance();
    }

    @Override
    public @NotNull <T> Optional<T> tryGet(@NotNull TypeIdentifier<T> typeIdentifier) {
        // Direct factory lookup
        BeanFactory<T> factory = (BeanFactory<T>) factories.get(typeIdentifier.erasure());
        if (factory == null) {
            return Optional.empty();
        }

        TypeIdentifier<T> erased = typeIdentifier.erasure();
        IdentifiableProvider<T> provider = factory.resolveProvider(null);
        if (provider == null) {
            return Optional.empty();
        }

        return scopeStore().getOrTrySet(provider, erased, () ->
                Optional.ofNullable(factory.get(requireWireContainer(), typeIdentifier.unqualified())).map(scopeCallback::newBeanCreated)
        ).map(Bean::instance);
    }

    @Override
    public <T> @NotNull Optional<T> tryGet(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        TypeIdentifier<T> rootType = qualifierType.type();

        // Always lookup by erasure
        BeanFactory<T> factory = (BeanFactory<T>) factories.get(rootType.erasure());
        if (factory == null) {
            return Optional.empty();
        }

        IdentifiableProvider<T> provider = factory.resolveProvider(qualifierType.qualifier());
        if (provider == null) {
            return Optional.empty();
        }

        WireContainer wireContainer = requireWireContainer();
        // Pass original qualified type to factory
        return scopeStore().getOrTrySet(provider, rootType, () ->
                Optional.ofNullable(factory.get(wireContainer, qualifierType)).map(scopeCallback::newBeanCreated)
        ).map(Bean::instance);
    }

    private <T> Bean<T> createNewBean(BeanFactory<T> factory, TypeIdentifier<T> typeIdentifier) {
        Bean<T> newBean = factory.get(requireWireContainer(), typeIdentifier);
        if (newBean == null) {
            throw MissingBeanException.unableToCreate(typeIdentifier);
        }
        return scopeCallback.newBeanCreated(newBean);
    }

    private <T> Bean<T> createNewBean(BeanFactory<T> factory, QualifiedTypeIdentifier<T> typeIdentifier) {
        Bean<T> newBean = factory.get(requireWireContainer(), typeIdentifier);
        if (newBean == null) {
            throw MissingBeanException.unableToCreate(typeIdentifier);
        }
        return scopeCallback.newBeanCreated(newBean);
    }

    @Override
    public <T> @NotNull Stream<Bean<T>> getAllBeans(@NotNull TypeIdentifier<T> type) {
        WireContainer wireContainer = requireWireContainer();

        return scopeStore().getAll(type, () -> {
                    Set<Bean<T>> result = new HashSet<>();

                    // Always lookup by erasure, but pass original type to factory
                    BeanFactory<T> factory = factories.get(type.erasure());
                    if (factory != null) {
                        result.addAll(factory.getAll(wireContainer, type));
                    }

                    return result;
                }).stream()
                .map(scopeCallback::newBeanCreated)
                .sorted(OrderedComparator.INSTANCE);
    }

    @Override
    public <T> @NotNull List<T> getAll(@NotNull TypeIdentifier<T> type) {
        return getAllBeans(type)
                .map(Bean::instance)
                .toList();
    }

    @Override
    public boolean contains(@NotNull QualifiedTypeIdentifier<?> type) {
        return factories.containsKey(type.type().erasure());
    }

    @Override
    public boolean contains(@NotNull TypeIdentifier<?> type) {
        return factories.containsKey(type.erasure());
    }

    @Override
    public boolean canSupply(@NotNull QualifiedTypeIdentifier<?> type) {
        return factories.containsKey(type.type().erasure());
    }

    @Override
    public boolean canSupply(@NotNull TypeIdentifier<?> type) {
        return factories.containsKey(type.erasure());
    }

    @Override
    public final void reset() {
        scopeCallback.scopeResetting(this);
        this.scopeStore().tearDown();
        doReset();
        scopeCallback.scopeReset(this);
    }

    protected void doReset() {
    }

    @Override
    public void link(@NotNull WireContainer wireContainer) {
        this.wireContainer = wireContainer;
    }

    @Override
    public void unlink() {
        this.wireContainer = null;
    }

    @NotNull
    protected WireContainer requireWireContainer() {
        if (wireContainer == null) {
            throw new IllegalStateException(this + " is not linked to a WireContainer");
        }
        return wireContainer;
    }

    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }
}