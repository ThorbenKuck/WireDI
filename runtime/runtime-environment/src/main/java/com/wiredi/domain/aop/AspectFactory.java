package com.wiredi.domain.aop;

import com.wiredi.aspects.AspectInstance;
import com.wiredi.runtime.WireRepository;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;

public interface AspectFactory<T extends Annotation> {

    @NotNull
    AspectInstance<T> build(@NotNull final WireRepository wireRepository);

    @NotNull
    Class<T> aroundAnnotation();

}
