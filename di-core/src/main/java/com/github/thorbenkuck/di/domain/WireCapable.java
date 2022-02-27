package com.github.thorbenkuck.di.domain;

import org.jetbrains.annotations.NotNull;

public interface WireCapable {

    @NotNull
    Class<?>[] wiredTypes();
}
