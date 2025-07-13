package com.wiredi.runtime.domain.provider;

import com.wiredi.runtime.WireContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jakarta.inject.Provider;
import java.util.Objects;

public final class WrappingProvider<T> implements Provider<T> {

    @NotNull
    private final IdentifiableProvider<T> provider;

    @NotNull
    private final WireContainer wireContainer;

    public WrappingProvider(
            @NotNull IdentifiableProvider<T> provider,
            @NotNull WireContainer wireContainer
    ) {
        this.provider = provider;
        this.wireContainer = wireContainer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public T get() {
        return provider.get(wireContainer, (TypeIdentifier<T>) provider.type());
    }

    /**
     * {@inheritDoc}
     *
     * A wrapping provider is equal to another Wrapping Provider, if:
     *
     * - Both are referencing the same {@link WireContainer}
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
        return wireContainer.equals(that.wireContainer) && provider.equals(that.provider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(provider, wireContainer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "WrappingProvider{" +
                "provider=" + provider +
                ", wireContainer=" + wireContainer +
                '}';
    }
}
