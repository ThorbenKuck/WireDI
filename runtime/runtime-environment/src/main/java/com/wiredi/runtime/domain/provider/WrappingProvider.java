package com.wiredi.runtime.domain.provider;

import com.wiredi.runtime.WireRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jakarta.inject.Provider;
import java.util.Objects;

public final class WrappingProvider<T> implements Provider<T> {

    @NotNull
    private final IdentifiableProvider<T> provider;

    @NotNull
    private final WireRepository wireRepository;

    public WrappingProvider(
            @NotNull IdentifiableProvider<T> provider,
            @NotNull WireRepository wireRepository
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
        return provider.get(wireRepository, (TypeIdentifier<T>) provider.type());
    }

    /**
     * {@inheritDoc}
     *
     * A wrapping provider is equal to another Wrapping Provider, if:
     *
     * - Both are referencing the same {@link WireRepository}
     * - Both have an equal native {@link IdentifiableProvider}
     *
     * Please note, that nearly all IdentifiableProviders should not override
     * the equals and hashcode methods, since they will only be loaded once
     * into the JVM.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WrappingProvider<?> that = (WrappingProvider<?>) o;
        return wireRepository.equals(that.wireRepository) && provider.equals(that.provider);
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
