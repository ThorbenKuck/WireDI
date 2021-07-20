package com.github.thorbenkuck.di;

import javax.inject.Provider;
import java.util.*;
import java.util.stream.Collectors;

public class WiredTypes extends SynchronizedServiceLoader<IdentifiableProvider> implements Repository {

    private final WiredTypesConfiguration configuration = new WiredTypesConfiguration();

    private final Map<Class<?>, List<IdentifiableProvider<?>>> mapping = new HashMap<>();

    public WiredTypes() {
        if (configuration.doDiAutoLoad()) {
            load();
        }
    }

    public WiredTypesConfiguration configuration() {
        return configuration;
    }

    @Override
    public void add(IdentifiableProvider provider) {
        dataAccess.write(() -> {
            for (Object wiredType : provider.wiredTypes()) {
                if (wiredType == null) {
                    throw new DiLoadingException("The provider " + provider + " returned null as an identifiable type! This is not permitted.\n" +
                            "If you did not create your own instance, please submit your annotated class to github.");
                }
                unsafeAdd((Class<?>) wiredType, provider);
            }
        });
    }

    public void unload() {
        mapping.clear();
        loaded = false;
    }

    @Override
    public void load() {
        add(new RepositoryIdentifyingProvider(this));
        super.load();
    }

    @Override
    public Class<IdentifiableProvider> serviceType() {
        return IdentifiableProvider.class;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        List<IdentifiableProvider<T>> providers = getAllProviders(type);
        if (providers.isEmpty()) {
            return null;
        }
        IdentifiableProvider<T> provider = findPrimaryProvider(providers, type);

        try {
            T t = provider.get(this);

            sanityCheckInstanceWithProduced(provider, t, type);
            return t;
        } catch (Exception e) {
            throw new DiInstantiationException("Error while letting the provider " + provider + " produce the correlating instance", e);
        }
    }

    @Override
    public <T> T requireInstance(Class<T> type) {
        IdentifiableProvider<T> provider = getSingleProvider(type);

        try {
            T t = provider.get(this);

            sanityCheckInstanceWithProduced(provider, t, type);
            return t;
        } catch (Exception e) {
            throw new DiInstantiationException("Error while letting the provider " + provider + " produce the correlating instance", e);
        }
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
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


    }

    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        IdentifiableProvider<T> provider = getSingleProvider(type);

        return new ProviderMapper<>(provider, this);
    }

    @Override
    public String toString() {
        return "WiredTypes{" +
                "mapping=" + mapping +
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

    private <T> IdentifiableProvider<T> getSingleProvider(Class<T> type) {
        List<IdentifiableProvider<T>> allProviders = getAllProviders(type);
        if (allProviders.isEmpty()) {
            throw new DiInstantiationException("No providers registered for the type " + type);
        }

        if (allProviders.size() > 1) {
            return findPrimaryProvider(allProviders, type);
        }

        return allProviders.get(0);
    }

    private <T> List<IdentifiableProvider<T>> getAllProviders(Class<T> type) {
        return dataAccess.read(() -> mapping.getOrDefault(type, Collections.emptyList())
                .stream()
                .map(it -> (IdentifiableProvider<T>) it)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    private void unsafeAdd(Class<?> type, IdentifiableProvider<?> provider) {
        List<IdentifiableProvider<?>> providers = mapping.computeIfAbsent(type, (t) -> new ArrayList<>());
        providers.add(provider);
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
