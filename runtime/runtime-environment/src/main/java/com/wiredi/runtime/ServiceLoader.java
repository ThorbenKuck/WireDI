package com.wiredi.runtime;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.WireRepositoryContextCallbacks;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.environment.resolvers.EnvironmentExpressionResolver;
import com.wiredi.runtime.time.Timed;
import com.wiredi.runtime.environment.EnvironmentConfiguration;
import com.wiredi.runtime.properties.loader.PropertyFileTypeLoader;
import com.wiredi.runtime.resources.ResourceProtocolResolver;
import com.wiredi.runtime.values.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.wiredi.runtime.lang.Ordered.ordered;

public class ServiceLoader {

    private final Logging LOGGER = Logging.getInstance(ServiceLoader.class);
    private final Value<List<WireRepositoryContextCallbacks>> contextCallbacks = Value.async(() -> ordered(load(WireRepositoryContextCallbacks.class)));
    private final Value<List<EnvironmentExpressionResolver>> environmentResolvers = Value.async(() -> ordered(load(EnvironmentExpressionResolver.class)));
    private final Value<List<EnvironmentConfiguration>> environmentConfigurations = Value.async(() -> ordered(load(EnvironmentConfiguration.class)));
    private final Value<List<ResourceProtocolResolver>> protocolResolvers = Value.async(() -> load(ResourceProtocolResolver.class));
    private final Value<List<PropertyFileTypeLoader>> propertyFileLoader = Value.async(() -> load(PropertyFileTypeLoader.class));

    private static ServiceLoader GLOBAL_INSTANCE = new ServiceLoader();

    public static ServiceLoader getInstance() {
        return GLOBAL_INSTANCE;
    }

    /**
     * This will load all the IdentifiableProviders that are registered.
     * <p>
     * It will always create a new list of classes, to not overflow states, as most IdentifiableProviders hold the state
     * of their instance.
     * <p>
     * This requires a bit more performance on load, though it prevents state issues.
     *
     * @return a newly loaded list of all registered IdentifiableProviders.
     */
    public List<IdentifiableProvider> identifiableProviders() {
        return ordered(load(IdentifiableProvider.class, true));
    }

    public List<WireRepositoryContextCallbacks> contextCallbacks() {
        return new ArrayList<>(contextCallbacks.get());
    }

    public List<EnvironmentExpressionResolver> environmentExpressionResolvers() {
        return new ArrayList<>(environmentResolvers.get());
    }

    public List<ResourceProtocolResolver> protocolResolvers() {
        return new ArrayList<>(protocolResolvers.get());
    }

    public List<EnvironmentConfiguration> environmentConfigurations() {
        return new ArrayList<>(environmentConfigurations.get());
    }

    public List<PropertyFileTypeLoader> propertyFileLoaders() {
        return new ArrayList<>(propertyFileLoader.get());
    }

    public <T> List<T> load(Class<T> type) {
        return load(type, false);
    }

    public <T> List<T> load(Class<T> type, Boolean ignoreClassNotFound) {
        final List<T> content = new ArrayList<>();
        LOGGER.trace(() -> "Starting to load " + type.getSimpleName());

        Timed timed;
        if (ignoreClassNotFound) {
            timed = Timed.of(() -> java.util.ServiceLoader.load(type)
                    .stream()
                    .forEach(provider -> {
                        try {
                            content.add(provider.get());
                        } catch (Throwable throwable) {
                            if (throwable.getCause() instanceof ClassNotFoundException) {
                                LOGGER.warn(() -> "The " + provider.type() + " provider could not be loaded, as it caused: " + throwable.getMessage(), throwable);
                            } else {
                                throw throwable;
                            }
                        }
                    }));
        } else {
            timed = Timed.of(() -> java.util.ServiceLoader.load(type).forEach(content::add));
        }

        LOGGER.trace(() -> "Loaded " + type.getSimpleName() + " in " + timed.get(TimeUnit.MILLISECONDS) + "ms");
        return content;
    }
}
