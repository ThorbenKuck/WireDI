package com.github.thorbenkuck.di.domain;

import com.github.thorbenkuck.di.WiredTypesConfiguration;
import com.github.thorbenkuck.di.properties.TypedProperties;
import com.github.thorbenkuck.di.aspects.AspectRepository;

import javax.inject.Provider;
import java.util.List;

public interface WireRepository {
    boolean isLoaded();

    TypedProperties properties();

    AspectRepository aspectRepository();

    WiredTypesConfiguration configuration();

    <T> void announce(T o);

    <T> T tryGetInstance(Class<T> type);

    <T> T getInstance(Class<T> type);

    <T> T requireInstance(Class<T> type);

    <T> List<T> getAll(Class<T> type);

    <T> Provider<T> getProvider(Class<T> type);
}
