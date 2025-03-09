package com.wiredi.runtime.domain.provider.sources;

import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.IdentifiableProviderSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FixedIdentifiableProviderSource implements IdentifiableProviderSource {

    private final List<IdentifiableProvider<?>> providers = new ArrayList<>();

    public FixedIdentifiableProviderSource(Collection<IdentifiableProvider<?>> providers) {
        this.providers.addAll(providers);
    }

    @Override
    public List<IdentifiableProvider<?>> load() {
        return providers;
    }
}
