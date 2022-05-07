package com.github.thorbenkuck.di.runtime;

import com.github.thorbenkuck.di.aspects.AspectRepository;
import com.github.thorbenkuck.di.domain.WireConflictResolver;
import com.github.thorbenkuck.di.domain.provider.IdentifiableProvider;
import com.github.thorbenkuck.di.runtime.exceptions.DiInstantiationException;
import com.github.thorbenkuck.di.runtime.properties.TypedProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WiredTypes extends SynchronizedServiceLoader<IdentifiableProvider> implements WireRepository {

    @NotNull
    private final WiredTypesConfiguration configuration = new WiredTypesConfiguration();

    @NotNull
    private final TypedProperties propertyContainer = new TypedProperties();

    @NotNull
    private final AspectRepository aspectRepository = new AspectRepository();

    @NotNull
    private final WireRepositoryCallbackRepository callbackRepository = new WireRepositoryCallbackRepository();

    private static final Logger logger = LoggerFactory.getLogger(WiredTypes.class);

    protected WiredTypes() {
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
        announce(IdentifiableProvider.singleton(o));
    }

    @Override
    public <T> void announce(@NotNull IdentifiableProvider<T> identifiableProvider) {
        register(identifiableProvider);
    }

    @Override
    public Timed load() {
        if(loaded) {
            throw new IllegalStateException("The WireRepository is already loaded");
        }

        Timed timed = Timed.of(() -> {
            logger.debug("Starting to setup WiredTypes");
            callbackRepository.forEach(callback -> callback.preLoading(this));
            register(IdentifiableProvider.singleton(this, WireRepository.class));
            register(IdentifiableProvider.singleton(aspectRepository, AspectRepository.class));
            super.load();
            callbackRepository.forEach(callback -> callback.postLoading(this));
            aspectRepository.load(this);
            callbackRepository.forEach(callback -> callback.postAspectLoading(this));
        });

        logger.info("Setup finished in {}ms", timed.get(TimeUnit.MILLISECONDS));
        return timed;
    }

    @Override
    public <T> Optional<T> tryGet(@NotNull final Class<T> type) {
        final List<IdentifiableProvider<T>> providers = getAllProviders(type);
        if (providers.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(get(type));
        } catch (DiInstantiationException e) {
            return Optional.empty();
        }
    }

    @Override
    @NotNull
    public <T> T get(@NotNull final Class<T> type) throws DiInstantiationException {
        final IdentifiableProvider<T> provider = requireSingleProvider(type);

        try {
            T t = instantiate(provider);

            sanityCheckInstanceWithProduced(provider, t, type);
            return t;
        } catch (final Exception e) {
            throw wireCreationError(e, type, provider);
        }
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
                        throw wireCreationError(e, type, provider);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @NotNull
    public <T> Provider<T> getProvider(@NotNull final Class<T> type) {
        final IdentifiableProvider<T> provider = requireSingleProvider(type);

        return wrapInProvider(provider);
    }

    @NotNull
    public <T> Provider<T> wrapInProvider(@NotNull final IdentifiableProvider<T> identifiableProvider) {
        return new WrappingProvider<>(identifiableProvider, this);
    }

    @Override
    @NotNull
    protected Class<IdentifiableProvider> serviceType() {
        return IdentifiableProvider.class;
    }

    @Nullable
    private <T> T instantiate(@NotNull final IdentifiableProvider<T> provider) {
        return provider.get(this);
    }

    @NotNull
    private DiInstantiationException wireCreationError(
            @NotNull final Exception e,
            @NotNull final Class<?> wireType,
            @NotNull final IdentifiableProvider<?> provider
    ) {
        return new DiInstantiationException("Error while wiring " + provider.type(), wireType, e);
    }

    @NotNull
    private <T> IdentifiableProvider<T> requireSingleProvider(@NotNull final Class<T> type) {
        List<IdentifiableProvider<T>> allProviders = getAllProviders(type);
        if (allProviders.isEmpty()) {
            throw new DiInstantiationException("Could not find any instance for " + type, type);
        }

        if(allProviders.size() == 1) {
            return allProviders.get(0);
        }

        return findPrimaryProvider(allProviders, type);
    }

    @NotNull
    private <T> IdentifiableProvider<T> findPrimaryProvider(
            @NotNull final List<IdentifiableProvider<T>> providers,
            @NotNull final Class<T> type
    ) {
        if (providers.isEmpty()) {
            throw new DiInstantiationException("No provider registered for type " + type, type);
        }
        if (providers.size() == 1) {
            return providers.get(0);
        }
        final WireConflictResolver wireConflictStrategy = configuration.conflictStrategy();
        return wireConflictStrategy.find(providers, type);
    }

    @NotNull
    private <T> List<IdentifiableProvider<T>> getAllProviders(@NotNull final Class<T> type) {
        return dataAccess.read(() -> unsafeGet(type)
                .stream()
                .map(it -> (IdentifiableProvider<T>) it)
                .collect(Collectors.toList()));
    }

    /**
     * This method checks, that created instances of {@link IdentifiableProvider} matches the provided types.
     *
     * If the
     *
     * @param provider
     * @param t
     * @param type
     * @param <T>
     */
    private <T> void sanityCheckInstanceWithProduced(
            @NotNull final IdentifiableProvider<T> provider,
            @Nullable final T t,
            @NotNull final Class<T> type
    ) {
        if (t == null) {
            throw new DiInstantiationException("Provider produced null for type " + type.getName() + ". This is not allowed by design!", type);
        }
        if(provider.bypassSanityCheck()) {
            return;
        }

        if (!provider.type().isAssignableFrom(t.getClass())) {
            throw new DiInstantiationException("The provider for the class " + type.getName() + " is not compatible with the produced instance " + t, type);
        }
    }

    private static final class WrappingProvider<T> implements Provider<T> {

        @NotNull
        private final IdentifiableProvider<T> provider;
        @NotNull
        private final WiredTypes wireRepository;

        private WrappingProvider(
                @NotNull IdentifiableProvider<T> provider,
                @NotNull WiredTypes wireRepository
        ) {
            this.provider = provider;
            this.wireRepository = wireRepository;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nullable
        public T get() {
            return provider.get(wireRepository);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WrappingProvider<?> that = (WrappingProvider<?>) o;
            return provider.equals(that.provider) && wireRepository.equals(that.wireRepository);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hash(provider, wireRepository);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "WrappingProvider{" +
                    "provider=" + provider +
                    ", wireRepository=" + wireRepository +
                    '}';
        }
    }
}
