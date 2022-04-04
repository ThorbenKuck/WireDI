package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.aspects.AspectRepository;
import com.github.thorbenkuck.di.domain.IdentifiableProvider;
import org.jetbrains.annotations.NotNull;

final class AspectRepositoryIdentifyingProvider implements IdentifiableProvider<AspectRepository> {

    @NotNull
    private static final Class<?>[] WIRED_TO = new Class[] { AspectRepository.class };

    @NotNull
    private final AspectRepository aspectRepository;

    AspectRepositoryIdentifyingProvider(@NotNull final AspectRepository aspectRepository) {
        this.aspectRepository = aspectRepository;
    }

    @Override
    @NotNull
    public Class<?> type() {
        return AspectRepository.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    @NotNull
    public AspectRepository get(@NotNull final WireRepository wiredTypes) {
        return aspectRepository;
    }

    @Override
    @NotNull
    public Class<?>[] wiredTypes() {
        return WIRED_TO;
    }
}
