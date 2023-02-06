package com.github.thorbenkuck.di.domain.provider;

import com.github.thorbenkuck.di.runtime.WiredTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.util.Objects;

public final class WrappingProvider<T> implements Provider<T> {

    @NotNull
    private final IdentifiableProvider<T> provider;
    @NotNull
    private final WiredTypes wireRepository;

    public WrappingProvider(
            @NotNull IdentifiableProvider<T> provider,
            @NotNull WiredTypes wireRepository
    ) {
        this.provider = provider;
        this.wireRepository = wireRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public T get() {
        return provider.get(wireRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WrappingProvider<?> that = (WrappingProvider<?>) o;
        return provider.equals(that.provider) && wireRepository.equals(that.wireRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(provider, wireRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "WrappingProvider{" +
                "provider=" + provider +
                ", wireRepository=" + wireRepository +
                '}';
    }
}