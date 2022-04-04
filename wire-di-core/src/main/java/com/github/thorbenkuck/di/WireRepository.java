package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.annotations.ManualWireCandidate;
import com.github.thorbenkuck.di.domain.IdentifiableProvider;
import com.github.thorbenkuck.di.properties.TypedProperties;
import com.github.thorbenkuck.di.aspects.AspectRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.util.List;
import java.util.Optional;

@ManualWireCandidate
public interface WireRepository {

    static WireRepository open() {
        WiredTypes wiredTypes = new WiredTypes();
        wiredTypes.load();

        return wiredTypes;
    }

    static WireRepository create() {
        return new WiredTypes();
    }

    boolean isLoaded();

    @NotNull
    TypedProperties properties();

    @NotNull
    AspectRepository aspectRepository();

    @NotNull
    WiredTypesConfiguration configuration();

    <T> void announce(@NotNull final T o);

    <T> void announce(@NotNull final IdentifiableProvider<T> identifiableProvider);

    @Nullable <T> Optional<T> tryGet(Class<T> type);

    @NotNull <T> T get(Class<T> type);

    <T> List<T> getAll(Class<T> type);

    <T> Provider<T> getProvider(Class<T> type);

    void load();
}
