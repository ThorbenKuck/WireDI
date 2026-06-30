package com.wiredi.runtime.domain.provider.sources;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.IdentifiableProviderSource;
import com.wiredi.runtime.lang.OrderedComparator;
import com.wiredi.runtime.services.DefaultServiceFileSource;
import com.wiredi.runtime.services.ServiceFileSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

/**
 * A source for {@link IdentifiableProvider} using the {@link ServiceLoader}.
 * <p>
 * This source is loading providers from service files.
 *
 * @see IdentifiableProviderSource
 * @see IdentifiableProvider
 * @see ServiceLoader
 */
public class ServiceLoaderIdentifiableProviderSource implements IdentifiableProviderSource {

    private static final Logging logger = Logging.getInstance(ServiceLoaderIdentifiableProviderSource.class);
    private final ServiceFileSource source;

    public ServiceLoaderIdentifiableProviderSource(ServiceFileSource source) {
        this.source = source;
    }

    public ServiceLoaderIdentifiableProviderSource() {
        this.source = DefaultServiceFileSource.INSTANCE;
    }

    @Override
    public Collection<IdentifiableProvider<?>> load() {
        final List<IdentifiableProvider<?>> content = new ArrayList<>();
        logger.trace(() -> "Starting to load IdentifiableProviders");
        this.source.loadServiceFiles(IdentifiableProvider.class).forEach(content::add);
        return OrderedComparator.sorted(content);
    }
}
