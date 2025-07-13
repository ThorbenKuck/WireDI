package com.wiredi.runtime.domain.scopes;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.BeanFactory;
import com.wiredi.runtime.domain.Scope;
import com.wiredi.runtime.domain.factories.Bean;
import com.wiredi.runtime.domain.factories.MissingBeanException;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.QualifiedTypeIdentifier;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.domain.scopes.cache.ScopeStore;
import com.wiredi.runtime.domain.scopes.exceptions.ScopeNotActivatedException;
import com.wiredi.runtime.lang.OrderedComparator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractScope implements Scope {

    @NotNull
    private final Map<@NotNull TypeIdentifier, @NotNull BeanFactory> factories;
    private boolean autostart;
    @Nullable
    private WireContainer wireRepository;
    private boolean isActive = false;

    public AbstractScope(boolean autostart, @NotNull Map<@NotNull TypeIdentifier, @NotNull BeanFactory> factories) {
        this.autostart = autostart;
        this.factories = factories;
    }

    @NotNull
    protected abstract ScopeStore scopeStore();

    @Override
    public @NotNull <T> IdentifiableProvider<T> getProvider(@NotNull TypeIdentifier<T> typeIdentifier) {
        checkActive();
        BeanFactory<T> factory = factories.get(typeIdentifier.erasure());
        if (factory != null) {
            IdentifiableProvider<T> provider = factory.resolveProvider(null);
            if (provider != null) {
                return provider;
            }
        }

        throw MissingBeanException.unableToCreate(typeIdentifier.erasure());
    }

    @Override
    public @NotNull <T> IdentifiableProvider<T> getProvider(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        checkActive();
        BeanFactory<T> factory = factories.get(qualifierType.type().erasure());
        if (factory != null) {
            IdentifiableProvider<T> provider = factory.resolveProvider(qualifierType.qualifier());
            if (provider != null) {
                return provider;
            }
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
        checkActive();

        // Direct factory lookup using cached erasure
        BeanFactory<T> factory = (BeanFactory<T>) factories.get(typeIdentifier.erasure());
        if (factory == null) {
            throw MissingBeanException.unableToCreate(typeIdentifier);
        }

        // Use cached erasure for scope store key too
        TypeIdentifier<T> erased = typeIdentifier.erasure();
        IdentifiableProvider<T> provider = factory.resolveProvider(null);
        if (provider == null) {
            throw MissingBeanException.unableToCreate(typeIdentifier);
        }

        Bean<T> bean = scopeStore().getOrSet(provider, typeIdentifier, () -> {
            Bean<T> newBean = factory.get(requireWireRepository(), typeIdentifier);
            if (newBean == null) {
                throw MissingBeanException.unableToCreate(typeIdentifier);
            }
            return newBean;
        });

        if (bean == null) {
            // Impossible, but the jvm is happier if we add the check
            throw MissingBeanException.unableToCreate(typeIdentifier);
        }
        return bean.instance();
    }

    @Override
    public <T> @NotNull T get(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        checkActive();
        WireContainer wireContainer = requireWireRepository();
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

        @Nullable
        Bean<T> bean = scopeStore().getOrSet(provider, rootType, () -> {
            Bean<T> newBean = factory.get(wireContainer, qualifierType);
            if (newBean == null) {
                throw MissingBeanException.unableToCreate(rootType);
            }
            return newBean;
        });

        if (bean == null) {
            throw MissingBeanException.unableToCreate(rootType);
        }

        return bean.instance();
    }

    @Override
    public @NotNull <T> Optional<T> tryGet(@NotNull TypeIdentifier<T> typeIdentifier) {
        if (!isActive) {
            return Optional.empty();
        }

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
                Optional.ofNullable(factory.get(requireWireRepository(), typeIdentifier))
        ).map(Bean::instance);
    }

    @Override
    public <T> @NotNull Optional<T> tryGet(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        checkActive();

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

        WireContainer wireContainer = requireWireRepository();
        // Pass original qualified type to factory
        return scopeStore().getOrTrySet(provider, rootType, () ->
                Optional.ofNullable(factory.get(wireContainer, qualifierType))
        ).map(Bean::instance);
    }

    private <T> @NotNull Bean<T> createNewInstance(@NotNull QualifiedTypeIdentifier<T> qualifierType) {
        BeanFactory<T> beanFactory = factories.get(qualifierType.type().erasure());
        if (beanFactory == null) {
            throw MissingBeanException.missingFactory(qualifierType.type());
        }

        WireContainer wireContainer = requireWireRepository();
        Bean<T> bean = beanFactory.get(wireContainer, qualifierType);
        if (bean == null) {
            throw MissingBeanException.unableToCreate(qualifierType.type());
        }

        return bean;
    }

    @Override
    public <T> @NotNull Stream<Bean<T>> getAllBeans(@NotNull TypeIdentifier<T> type) {
        if (!this.isActive) {
            return Stream.empty();
        }
        WireContainer wireContainer = requireWireRepository();

        return scopeStore().getAll(type, () -> {
                    Set<Bean<T>> result = new HashSet<>();

                    // Always lookup by erasure, but pass original type to factory
                    BeanFactory<T> factory = factories.get(type.erasure());
                    if (factory != null) {
                        result.addAll(factory.getAll(wireContainer, type));
                    }

                    return result;
                }).stream()
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
    public void autostart() {
        if (autostart) {
            start();
        }
    }

    @Override
    public final void start() {
        doStart();
        this.isActive = true;
    }

    protected void doStart() {
    }

    @Override
    public final void finish() {
        this.scopeStore().tearDown();
        this.isActive = false;
        doFinish();
    }

    public void setAutostart(boolean autostart) {
        this.autostart = autostart;
    }

    protected void doFinish() {
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void link(@NotNull WireContainer wireRepository) {
        this.wireRepository = wireRepository;
    }

    @Override
    public void unlink() {
        this.wireRepository = null;
    }

    protected void checkActive() {
        if (!isActive) {
            throw new ScopeNotActivatedException(this);
        }
    }

    @NotNull
    protected WireContainer requireWireRepository() {
        if (wireRepository == null) {
            throw new IllegalStateException("Wire repository is not linked to scope");
        }
        return wireRepository;
    }

    public String toString() {
        return this.getClass().getSimpleName() + "{active=" + isActive + '}';
    }
}