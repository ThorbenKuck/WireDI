package com.wiredi.runtime;

import com.wiredi.domain.WireRepositoryContextCallbacks;
import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.environment.resolvers.EnvironmentExpressionResolver;
import com.wiredi.lang.values.FutureValue;
import com.wiredi.lang.time.Timed;
import com.wiredi.environment.EnvironmentConfiguration;
import com.wiredi.properties.loader.PropertyFileTypeLoader;
import com.wiredi.resources.ResourceProtocolResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

import static com.wiredi.domain.Ordered.ordered;

public class Loader {

    private static final Logger logger = LoggerFactory.getLogger(Loader.class);
    private static final FutureValue<List<WireRepositoryContextCallbacks>> contextCallbacks = FutureValue.of(() -> ordered(load(WireRepositoryContextCallbacks.class)));
    private static final FutureValue<List<EnvironmentExpressionResolver>> environmentResolvers = FutureValue.of(() -> load(EnvironmentExpressionResolver.class));
    private static final FutureValue<List<ResourceProtocolResolver>> protocolResolvers = FutureValue.of(() -> load(ResourceProtocolResolver.class));
    private static final FutureValue<List<PropertyFileTypeLoader>> propertyFileLoader = FutureValue.of(() -> load(PropertyFileTypeLoader.class));
    private static final FutureValue<List<EnvironmentConfiguration>> environmentConfigurations = FutureValue.of(() -> ordered(load(EnvironmentConfiguration.class)));

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
    public static List<IdentifiableProvider> identifiableProviders() {
        return ordered(load(IdentifiableProvider.class, true));
    }

    public static List<WireRepositoryContextCallbacks> contextCallbacks() {
        return contextCallbacks.get();
    }

    public static List<EnvironmentExpressionResolver> environmentExpressionResolvers() {
        return environmentResolvers.get();
    }

    public static List<ResourceProtocolResolver> protocolResolvers() {
        return protocolResolvers.get();
    }

    public static List<EnvironmentConfiguration> environmentConfigurations() {
        return environmentConfigurations.get();
    }

    public static List<PropertyFileTypeLoader> propertyFileLoaders() {
        return propertyFileLoader.get();
    }

    public static <T> List<T> load(Class<T> type) {
        return load(type, false);
    }

    public static <T> List<T> load(Class<T> type, Boolean ignoreClassNotFound) {
        final List<T> content = new ArrayList<>();
        logger.trace("Starting to load {}", type.getSimpleName());

        Timed timed;
        if (ignoreClassNotFound) {
            timed = Timed.of(() -> ServiceLoader.load(type)
                    .stream()
                    .forEach(provider -> {
                        try {
                            content.add(provider.get());
                        } catch (Throwable throwable) {
                            if (throwable.getCause() instanceof ClassNotFoundException) {
                                logger.atWarn()
                                        .setCause(throwable)
                                        .log(() -> "The " + provider.type() + " provider could not be loaded, as it caused: " + throwable.getMessage());
                            } else {
                                throw throwable;
                            }
                        }
                    }));
        } else {
            timed = Timed.of(() -> ServiceLoader.load(type).forEach(content::add));
        }

        logger.trace("Loaded {} in {}ms", type.getSimpleName(), timed.get(TimeUnit.MILLISECONDS));
        return content;
    }
}
