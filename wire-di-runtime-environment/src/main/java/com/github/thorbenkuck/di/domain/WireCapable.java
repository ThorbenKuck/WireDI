package com.github.thorbenkuck.di.domain;

import com.github.thorbenkuck.di.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

public interface WireCapable {

    @NotNull
    TypeIdentifier<?>[] wiredTypes();
}
