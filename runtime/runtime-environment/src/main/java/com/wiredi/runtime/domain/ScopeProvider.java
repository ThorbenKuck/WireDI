package com.wiredi.runtime.domain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ScopeProvider {

    @Nullable
    Scope getScope(@NotNull ScopeRegistry registry);

}
