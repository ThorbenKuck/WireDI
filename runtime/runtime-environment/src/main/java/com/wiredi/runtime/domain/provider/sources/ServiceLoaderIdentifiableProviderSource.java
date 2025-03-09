package com.wiredi.runtime.domain.provider.sources;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.IdentifiableProviderSource;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * A source for {@link IdentifiableProvider} using the {@link ServiceLoader}.
 * <p>
 * This source is loading providers from service files.
 *
 * @see com.wiredi.runtime.beans.BeanContainer
 * @see IdentifiableProviderSource
 * @see IdentifiableProvider
 * @see ServiceLoader
 */
public class ServiceLoaderIdentifiableProviderSource implements IdentifiableProviderSource {

    private static final Logging logger = Logging.getInstance(ServiceLoaderIdentifiableProviderSource.class);
    private final ServiceLoader<IdentifiableProvider> serviceLoader;
    private boolean ignoreClassNotFound = false;

    public ServiceLoaderIdentifiableProviderSource(ServiceLoader<IdentifiableProvider> serviceLoader) {
        this.serviceLoader = serviceLoader;
    }

    public ServiceLoaderIdentifiableProviderSource() {
        this(ServiceLoader.load(IdentifiableProvider.class));
    }

    @Override
    public List<IdentifiableProvider<?>> load() {
        final List<IdentifiableProvider<?>> content = new ArrayList<>();
        logger.trace(() -> "Starting to load IdentifiableProviders");
        if (ignoreClassNotFound) {
            serviceLoader.stream()
                    .forEach(provider -> {
                        try {
                            content.add(provider.get());
                        } catch (Throwable throwable) {
                            if (throwable.getCause() instanceof ClassNotFoundException) {
                                logger.warn(() -> "The " + provider.type() + " provider could not be loaded, as it caused: " + throwable.getMessage(), throwable);
                            } else {
                                throw throwable;
                            }
                        }
                    });
        } else {
            serviceLoader.forEach(content::add);
        }

        return content;
    }
}
