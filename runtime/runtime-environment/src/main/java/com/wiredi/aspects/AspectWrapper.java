package com.wiredi.aspects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

public class AspectWrapper<T extends Annotation> {

    @NotNull
    private final AspectInstance<T> rootAspectInstance;
    @Nullable
    private AspectWrapper<T> next;

    AspectWrapper(@NotNull AspectInstance<T> rootAspectInstance) {
        this.rootAspectInstance = rootAspectInstance;
    }

    @NotNull
    AspectWrapper<T> prepend(final @NotNull AspectInstance<T> aspectInstance) {
        final AspectWrapper<T> returnValue = new AspectWrapper<>(aspectInstance);
        returnValue.next = this;
        return returnValue;
    }

    @NotNull
    public AspectInstance<T> getRootAspect() {
        return rootAspectInstance;
    }

    @Nullable
    public AspectWrapper<T> getNext() {
        return next;
    }
}