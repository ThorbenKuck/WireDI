package com.github.thorbenkuck.di.domain;

import com.github.thorbenkuck.di.aspects.AspectInstance;

import java.lang.annotation.Annotation;

public interface AspectFactory<T extends Annotation> {

    AspectInstance<T> build(WireRepository wireRepository);

    Class<T> aroundAnnotation();

}
