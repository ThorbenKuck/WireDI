package com.github.thorbenkuck.di.runtime;

import com.github.thorbenkuck.di.aspects.AspectRepository;
import com.github.thorbenkuck.di.domain.Eager;
import com.github.thorbenkuck.di.domain.WireConflictResolver;
import com.github.thorbenkuck.di.domain.provider.TypeIdentifier;
import com.github.thorbenkuck.di.domain.provider.IdentifiableProvider;
import com.github.thorbenkuck.di.domain.provider.WrappingProvider;
import com.github.thorbenkuck.di.runtime.beans.BeanContainer;
import com.github.thorbenkuck.di.runtime.exceptions.DiInstantiationException;
import com.github.thorbenkuck.di.runtime.properties.TypedProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.sql.Time;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WiredTypes implements WireRepository {

    @NotNull
    private final WiredTypesConfiguration configuration = new WiredTypesConfiguration();

    @NotNull
    private final TypedProperties propertyContainer = new TypedProperties();

    @NotNull
    private final AspectRepository aspectRepository = new AspectRepository();

    @NotNull
    private final WireRepositoryCallbackRepository callbackRepository = new WireRepositoryCallbackRepository();

    @NotNull
    private final BeanContainer<IdentifiableProvider> beanContainer = new BeanContainer<>(IdentifiableProvider.class);

    private static final Logger logger = LoggerFactory.getLogger(WiredTypes.class);

    protected WiredTypes() {
    }

    @Override
    public boolean isLoaded() {
        return beanContainer.isLoaded();
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
        beanContainer.register(identifiableProvider);
    }

    @Override
    public Timed load() {
        if (isLoaded()) {
            throw new IllegalStateException("The WireRepository is already loaded");
        }

        Timed setupPhase = Timed.of(() -> {
            logger.debug("Starting to setup WiredTypes");
            callbackRepository.forEach(callback -> callback.preLoading(this));
            beanContainer.register(IdentifiableProvider.singleton(this, WireRepository.class));
            beanContainer.register(IdentifiableProvider.singleton(aspectRepository, AspectRepository.class));
            beanContainer.load();
            callbackRepository.forEach(callback -> callback.postLoading(this));
            aspectRepository.load(this);
            callbackRepository.forEach(callback -> callback.postAspectLoading(this));
        });
        logger.info("Setup finished in {}", setupPhase);


        List<Eager> eagerInstances = getAll(Eager.class);
        if(eagerInstances.isEmpty()) {
            logger.info("Setup finished in {}", setupPhase);
            return setupPhase;
        }
        logger.info("Setup done in {}", setupPhase);
        Timed eagerPhase = Timed.of(() -> {
            eagerInstances.parallelStream()
                    .forEach(it -> it.setup(this));
        });

        logger.info("Eager class setup finished in {}", eagerPhase);
        Timed totalTime = setupPhase.plus(eagerPhase);
        logger.info("Setup finished in {}", totalTime);
        return totalTime;
    }

    @Override
    public <T> Optional<T> tryGet(@NotNull final Class<T> type) {
        final List<IdentifiableProvider> providers = beanContainer.getAll(type);
        if (providers.isEmpty()) {
            return Optional.empty();
        }

        try {
            IdentifiableProvider<T> primaryProvider = findPrimaryProvider(providers, type);
            T instance = instantiate(primaryProvider, type);
            return Optional.ofNullable(instance);
        } catch (DiInstantiationException e) {
            return Optional.empty();
        }
    }

    @Override
    @NotNull
    public <T> T get(@NotNull final Class<T> type) throws DiInstantiationException {
        List<IdentifiableProvider> matchingGenericProviders = beanContainer.getAll(type);
        IdentifiableProvider<T> provider = findPrimaryProvider(matchingGenericProviders, type);
        return instantiateNotNull(provider, type);
    }

    @Override
    @NotNull
    public <T> T get(@NotNull TypeIdentifier<T> identifier) throws DiInstantiationException {
        List<IdentifiableProvider> matchingGenericProviders = beanContainer.getAll(identifier);
        IdentifiableProvider<T> provider = findPrimaryProvider(matchingGenericProviders, identifier.getRootType());
        return instantiateNotNull(provider, identifier.getRootType());
    }

    @Override
    @NotNull
    public <T> List<T> getAll(@NotNull final Class<T> type) {
        return beanContainer.stream(type)
                .sorted()
                .map(provider -> {
                    try {
                        return (T) provider.get(this);
                    } catch (final Exception e) {
                        throw wireCreationError(e, type, provider);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @NotNull
    public <T> Stream<IdentifiableProvider<T>> stream(@NotNull final Class<T> type) {
        return beanContainer.stream(type)
                .sorted()
                .map(it -> (IdentifiableProvider<T>) it);
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

    @Nullable
    private <T> T instantiate(@NotNull final IdentifiableProvider<T> provider, Class<T> type) {
        try {
            T t = provider.get(this);

            sanityCheckInstanceWithProduced(provider, t, type);
            return t;
        } catch (final Exception e) {
            throw wireCreationError(e, type, provider);
        }
    }

    @NotNull
    private <T> T instantiateNotNull(@NotNull final IdentifiableProvider<T> provider, Class<T> type) {
        T instance = instantiate(provider, type);
        if (instance == null) {
            throw new DiInstantiationException("Provider produced null for type " + type.getName() + ". This is not allowed by design!", type);
        }
        return instance;
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
        List<IdentifiableProvider> allProviders = beanContainer.getAll(type);
        if (allProviders.isEmpty()) {
            throw new DiInstantiationException("Could not find any instance for " + type, type);
        }

        if (allProviders.size() == 1) {
            return (IdentifiableProvider<T>) allProviders.get(0);
        }

        return findPrimaryProvider(allProviders, type);
    }

    @NotNull
    private <T> IdentifiableProvider<T> findPrimaryProvider(
            @NotNull final List<IdentifiableProvider> providers,
            @NotNull final Class<T> type
    ) {
        if (providers.isEmpty()) {
            throw new DiInstantiationException("No provider registered for type " + type, type);
        }
        if (providers.size() == 1) {
            return (IdentifiableProvider<T>) providers.get(0);
        }
        final WireConflictResolver wireConflictStrategy = configuration.conflictStrategy();
        List<IdentifiableProvider<T>> generified = providers.stream()
                .map(it -> (IdentifiableProvider<T>) it)
                .collect(Collectors.toList());
        return wireConflictStrategy.find(generified, type);
    }

    /**
     * This method checks, that created instances of {@link IdentifiableProvider} matches the provided types.
     * <p>
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
            return;
        }
        if (provider.bypassSanityCheck()) {
            return;
        }

        if (!provider.type().isAssignableFrom(t.getClass())) {
            throw new DiInstantiationException("The provider for the class " + type.getName() + " is not compatible with the produced instance " + t, type);
        }
    }
}
