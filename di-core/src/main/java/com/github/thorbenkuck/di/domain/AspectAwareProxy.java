package com.github.thorbenkuck.di.domain;

import org.jetbrains.annotations.Nullable;

public interface AspectAwareProxy {

    static boolean isProxy(@Nullable final Object instance) {
        return instance instanceof AspectAwareProxy;
    }

}
