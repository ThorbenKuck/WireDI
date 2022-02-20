package com.github.thorbenkuck.di.aspects;

import java.lang.annotation.Annotation;

public interface AspectInstance<T extends Annotation> {

    Object process(ExecutionContext<T> context);

}
