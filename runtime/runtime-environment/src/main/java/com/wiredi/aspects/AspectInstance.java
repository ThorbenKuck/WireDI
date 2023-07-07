package com.wiredi.aspects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

public interface AspectInstance<T extends Annotation> {

    @Nullable
    Object process(@NotNull final ExecutionContext<T> context);

}
