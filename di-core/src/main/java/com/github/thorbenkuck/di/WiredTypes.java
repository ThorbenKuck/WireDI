package com.github.thorbenkuck.di;

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

public class WiredTypes extends SynchronizedServiceLoader<IdentifiableProvider> implements WireRepository {

    private final WiredTypesConfiguration configuration = new WiredTypesConfiguration();
    private final TypedProperties propertyContainer = new TypedProperties();
    private final AspectRepository aspectRepository = new AspectRepository();

    public WiredTypes() {
        if (configuration.doDiAutoLoad()) {
            load();
        }
    }

    @Override
    public TypedProperties properties() {
        return this.propertyContainer;
    }

    @Override
    public AspectRepository aspectRepository() {
        return aspectRepository;
    }

    @Override
    public WiredTypesConfiguration configuration() {
        return configuration;
    }

    @Override
    public <T> void announce(T o) {
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
    public Class<IdentifiableProvider> serviceType() {
        return IdentifiableProvider.class;
    }

    @Override
    @Nullable
    public <T> T tryGetInstance(Class<T> type) {
        try {
            return getInstance(type);
        } catch (DiInstantiationException e) {
            return null;
        }
    }

    @Override
    @Nullable
    public <T> T getInstance(Class<T> type) {
        List<IdentifiableProvider<T>> providers = getAllProviders(type);
        if (providers.isEmpty()) {
            return null;
        }
        IdentifiableProvider<T> provider = findPrimaryProvider(providers, type);

        try {
            T t = instantiate(provider, type);

            sanityCheckInstanceWithProduced(provider, t, type);
            return t;
        } catch (Exception e) {
            throw wireCreationError(e, provider);
        }
    }

    @Override
    @NotNull
    public <T> T requireInstance(Class<T> type) {
        IdentifiableProvider<T> provider = requireSingleProvider(type);

        try {
            T t = instantiate(provider, type);

            sanityCheckInstanceWithProduced(provider, t, type);
            return t;
        } catch (Exception e) {
            throw wireCreationError(e, provider);
        }
    }

    private <T> T instantiate(IdentifiableProvider<T> provider, Class<T> type) {
        return provider.get(this);
    }

    private DiInstantiationException wireCreationError(Exception e, IdentifiableProvider<?> provider) {
        return new DiInstantiationException("Error while wiring " + provider.type(), e);
    }

    @Override
    public <T> List<T> getAll(Class<T> type) {
        return getAllProviders(type)
                .stream()
                .sorted()
                .map(provider -> {
                    try {
                        return provider.get(this);
                    } catch (Exception e) {
                        throw new DiInstantiationException("Error instantiating the class " + provider.type().getSimpleName() + ".", e);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


    }

    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        IdentifiableProvider<T> provider = requireSingleProvider(type);

        return new ProviderMapper<>(provider, this);
    }

    @Override
    public String toString() {
        return "WiredTypes{" +
                ", loaded=" + loaded +
                '}';
    }

    private <T> IdentifiableProvider<T> findPrimaryProvider(List<IdentifiableProvider<T>> providers, Class<T> type) {
        if(providers.size() == 1) {
            return providers.get(0);
        }
        WireConflictStrategy wireConflictStrategy = configuration.conflictStrategy();
        return wireConflictStrategy.find(providers, type);
    }

    private <T> void sanityCheckInstanceWithProduced(IdentifiableProvider<T> provider, T t, Class<T> type) {
        if (t == null) {
            throw new DiInstantiationException("Provider produced null. This is not allowed by design!");
        }

        if (!provider.type().isAssignableFrom(t.getClass())) {
            throw new DiInstantiationException("The provider for the class " + type + " is not compatible with the produced instance " + t);
        }
    }

    private <T> IdentifiableProvider<T> requireSingleProvider(Class<T> type) {
        List<IdentifiableProvider<T>> allProviders = getAllProviders(type);
        if (allProviders.isEmpty()) {
            throw new DiInstantiationException("Could not find any instance for " + type);
        }

        if (allProviders.size() > 1) {
            return findPrimaryProvider(allProviders, type);
        }

        return allProviders.get(0);
    }

    private <T> List<IdentifiableProvider<T>> getAllProviders(Class<T> type) {
        return dataAccess.read(() -> unsafeGet(type)
                .stream()
                .map(it -> (IdentifiableProvider<T>) it)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    private static final class ProviderMapper<T> implements Provider<T> {

        private final IdentifiableProvider<T> provider;
        private final WiredTypes wiredTypes;

        private ProviderMapper(IdentifiableProvider<T> provider, WiredTypes wiredTypes) {
            this.provider = provider;
            this.wiredTypes = wiredTypes;
        }

        @Override
        public T get() {
            return provider.get(wiredTypes);
        }
    }
}
