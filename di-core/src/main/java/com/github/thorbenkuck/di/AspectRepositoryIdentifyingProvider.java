package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.aspects.AspectRepository;
import com.github.thorbenkuck.di.domain.IdentifiableProvider;
import com.github.thorbenkuck.di.domain.WireRepository;

class AspectRepositoryIdentifyingProvider implements IdentifiableProvider<AspectRepository> {

    private static final Class<?>[] WIRED_TO = new Class[] { AspectRepository.class };
    private final AspectRepository aspectRepository;

    AspectRepositoryIdentifyingProvider(AspectRepository aspectRepository) {
        this.aspectRepository = aspectRepository;
    }

    @Override
    public Class<?> type() {
        return AspectRepository.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public AspectRepository get(WireRepository wiredTypes) {
        return aspectRepository;
    }

    @Override
    public Class<?>[] wiredTypes() {
        return WIRED_TO;
    }
}
