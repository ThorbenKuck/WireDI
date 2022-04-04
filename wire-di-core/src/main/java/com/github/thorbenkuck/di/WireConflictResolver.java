package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.domain.IdentifiableProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WireConflictResolver {
    @NotNull
    <T> IdentifiableProvider<T> find(
            @NotNull final List<IdentifiableProvider<T>> providerList,
            @NotNull final Class<T> expectedType
    );

    String name();
}
