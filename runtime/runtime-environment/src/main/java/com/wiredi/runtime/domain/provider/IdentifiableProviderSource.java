package com.wiredi.runtime.domain.provider;

import com.wiredi.runtime.domain.provider.sources.FixedIdentifiableProviderSource;
import com.wiredi.runtime.domain.provider.sources.ServiceLoaderIdentifiableProviderSource;
import com.wiredi.runtime.services.ServiceFileSource;

import java.util.Arrays;
import java.util.Collection;

/**
 * This interface abstracts how {@link IdentifiableProvider} instances are loaded.
 * <p>
 * The goal is to abstract how and from where the providers are loaded that are used by the {@link com.wiredi.runtime.WireContainerInitializer}
 * to construct beans.
 * Each different implementation can be understood as a strategy on how to load providers.
 * <p>
 * The most important source is the {@link com.wiredi.runtime.domain.provider.sources.ServiceLoaderIdentifiableProviderSource},
 * which uses the {@link java.util.ServiceLoader} to load all IdentifiableProviders and bypass reflections as much as possible.
 *
 * @see IdentifiableProvider
 * @see com.wiredi.runtime.WireContainerInitializer
 */
public interface IdentifiableProviderSource {

    static IdentifiableProviderSource serviceLoader(ServiceFileSource source) {
        return new ServiceLoaderIdentifiableProviderSource(source);
    }

    static IdentifiableProviderSource serviceLoader() {
        return new ServiceLoaderIdentifiableProviderSource();
    }

    static IdentifiableProviderSource just(IdentifiableProvider<?>... providers) {
        return new FixedIdentifiableProviderSource(Arrays.asList(providers));
    }

    static IdentifiableProviderSource just(Collection<IdentifiableProvider<?>> providers) {
        return new FixedIdentifiableProviderSource(providers);
    }

    /**
     * Load all {@link IdentifiableProvider} through the definition of this implementation
     *
     * @return all identifiable providers this source can identify
     */
    Collection<IdentifiableProvider<?>> load();
}
