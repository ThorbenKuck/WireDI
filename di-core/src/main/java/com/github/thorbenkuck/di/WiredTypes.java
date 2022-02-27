package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.annotations.ManualWireCandidate;
import com.github.thorbenkuck.di.domain.GenericIdentifyingProvider;
import com.github.thorbenkuck.di.domain.IdentifiableProvider;
import com.github.thorbenkuck.di.domain.WireRepository;
import com.github.thorbenkuck.di.properties.TypedProperties;
import com.github.thorbenkuck.di.aspects.AspectRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.util.*;
import java.util.stream.Collectors;

@ManualWireCandidate
public final class WiredTypes extends SynchronizedServiceLoader<IdentifiableProvider> implements WireRepository {

    @NotNull
    private final WiredTypesConfiguration configuration = new WiredTypesConfiguration();

    @NotNull
    private final TypedProperties propertyContainer = new TypedProperties();

    @NotNull
    private final AspectRepository aspectRepository = new AspectRepository();

    public WiredTypes() {
        if (configuration.doDiAutoLoad()) {
            load();
        }
    }

    @Override
    @NotNull
    public TypedProperties properties() {
        return this.propertyContainer;
    }

    @Override
    @NotNull
    public AspectRepository aspectRepository() {
        return aspectRepository;
    }

    @Override
    @NotNull
    public WiredTypesConfiguration configuration() {
        return configuration;
    }

    @Override
    public <T> void announce(@NotNull final T o) {
        register(new GenericIdentifyingProvider<>(o));
    }

    @Override
    public void load() {
        register(new RepositoryIdentifyingProvider(this));
        register(new AspectRepositoryIdentifyingProvider(aspectRepository));
        super.load();
        aspectRepository.load(this);
    }

    @Override
    @NotNull
    public Class<IdentifiableProvider> serviceType() {
        return IdentifiableProvider.class;
    }

    @Override
    @Nullable
    public <T> T tryGet(@NotNull final Class<T> type) {
        final List<IdentifiableProvider<T>> providers = getAllProviders(type);
        if (providers.isEmpty()) {
            return null;
        }

        try {
            return get(type);
        } catch (DiInstantiationException e) {
            return null;
        }
    }

    @Override
    @NotNull
    public <T> T get(@NotNull final Class<T> type) {
        final IdentifiableProvider<T> provider = requireSingleProvider(type);

        try {
            T t = instantiate(provider);

            sanityCheckInstanceWithProduced(provider, t, type);
            return t;
        } catch (final Exception e) {
            throw wireCreationError(e, provider);
        }
    }

    @Nullable
    private <T> T instantiate(@NotNull final IdentifiableProvider<T> provider) {
        return provider.get(this);
    }

    @NotNull
    private DiInstantiationException wireCreationError(
            @NotNull final Exception e,
            @NotNull final IdentifiableProvider<?> provider
    ) {
        return new DiInstantiationException("Error while wiring " + provider.type(), e);
    }

    @Override
    @NotNull
    public <T> List<T> getAll(@NotNull final Class<T> type) {
        return getAllProviders(type)
                .stream()
                .sorted()
                .map(provider -> {
                    try {
                        return provider.get(this);
                    } catch (final Exception e) {
                        throw wireCreationError(e, provider);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @NotNull
    public <T> Provider<T> getProvider(@NotNull final Class<T> type) {
        final IdentifiableProvider<T> provider = requireSingleProvider(type);

        return new ProviderMapper<>(provider, this);
    }

    @NotNull
    private <T> IdentifiableProvider<T> findPrimaryProvider(
            @NotNull final List<IdentifiableProvider<T>> providers,
            @NotNull final Class<T> type
    ) {
        if(providers.isEmpty()) {
            throw new DiInstantiationException("No provider registered for type " + type);
        }
        if(providers.size() == 1) {
            return providers.get(0);
        }
        final WireConflictStrategy wireConflictStrategy = configuration.conflictStrategy();
        return wireConflictStrategy.find(providers, type);
    }

    @NotNull
    private <T> IdentifiableProvider<T> requireSingleProvider(@NotNull final Class<T> type) {
        List<IdentifiableProvider<T>> allProviders = getAllProviders(type);
        if (allProviders.isEmpty()) {
            throw new DiInstantiationException("Could not find any instance for " + type);
        }

        if (allProviders.size() > 1) {
            return findPrimaryProvider(allProviders, type);
        }

        return allProviders.get(0);
    }

    @NotNull
    private <T> List<IdentifiableProvider<T>> getAllProviders(@NotNull final Class<T> type) {
        return dataAccess.read(() -> unsafeGet(type)
                .stream()
                .map(it -> (IdentifiableProvider<T>) it)
                .collect(Collectors.toList()));
    }

    private <T> void sanityCheckInstanceWithProduced(
            @NotNull final IdentifiableProvider<T> provider,
            @Nullable final T t,
            @NotNull final Class<T> type
    ) {
        if (t == null) {
            throw new DiInstantiationException("Provider produced null for type " + type.getName() + ". This is not allowed by design!");
        }

        if (!provider.type().isAssignableFrom(t.getClass())) {
            throw new DiInstantiationException("The provider for the class " + type + " is not compatible with the produced instance " + t);
        }
    }

    private static final class ProviderMapper<T> implements Provider<T> {

        @NotNull
        private final IdentifiableProvider<T> provider;
        @NotNull
        private final WiredTypes wiredTypes;

        private ProviderMapper(
                @NotNull IdentifiableProvider<T> provider,
                @NotNull WiredTypes wiredTypes
        ) {
            this.provider = provider;
            this.wiredTypes = wiredTypes;
        }

        @Override
        @Nullable
        public T get() {
            return provider.get(wiredTypes);
        }
    }
}
