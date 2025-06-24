package com.wiredi.runtime.domain.provider.sources;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.ServiceFiles;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.IdentifiableProviderSource;
import com.wiredi.runtime.lang.OrderedComparator;

import java.util.ArrayList;
import java.util.Collection;
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
    private final ServiceFiles<IdentifiableProvider> serviceLoader;

    public ServiceLoaderIdentifiableProviderSource(ServiceFiles<IdentifiableProvider> serviceLoader) {
        this.serviceLoader = serviceLoader;
    }

    public ServiceLoaderIdentifiableProviderSource() {
        this.serviceLoader = ServiceFiles.getInstance(IdentifiableProvider.class);
    }

    @Override
    public Collection<IdentifiableProvider<?>> load() {
        final List<IdentifiableProvider<?>> content = new ArrayList<>();
        logger.trace(() -> "Starting to load IdentifiableProviders");
        serviceLoader.instances().forEach(content::add);
        return OrderedComparator.sorted(content);
    }
}
