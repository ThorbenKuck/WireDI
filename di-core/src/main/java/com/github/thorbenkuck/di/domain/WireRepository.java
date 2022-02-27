package com.github.thorbenkuck.di.domain;

import com.github.thorbenkuck.di.WiredTypesConfiguration;
import com.github.thorbenkuck.di.properties.TypedProperties;
import com.github.thorbenkuck.di.aspects.AspectRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.util.List;

public interface WireRepository {
    boolean isLoaded();

    @NotNull
    TypedProperties properties();

    @NotNull
    AspectRepository aspectRepository();

    @NotNull
    WiredTypesConfiguration configuration();

    <T> void announce(@NotNull final T o);

    @Nullable <T> T tryGet(Class<T> type);

    @NotNull <T> T get(Class<T> type);

    <T> List<T> getAll(Class<T> type);

    <T> Provider<T> getProvider(Class<T> type);
}
