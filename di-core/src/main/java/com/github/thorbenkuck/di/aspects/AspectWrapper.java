package com.github.thorbenkuck.di.aspects;

import java.lang.annotation.Annotation;

public class AspectWrapper<T extends Annotation> {

    private final AspectInstance<T> rootAspectInstance;
    private AspectWrapper<T> next;

    AspectWrapper(AspectInstance<T> rootAspectInstance) {
        this.rootAspectInstance = rootAspectInstance;
    }

    AspectWrapper<T> prepend(AspectInstance<T> aspectInstance) {
        AspectWrapper<T> returnValue = new AspectWrapper<>(aspectInstance);
        returnValue.next = this;
        return returnValue;
    }

    public AspectInstance<T> getRootAspect() {
        return rootAspectInstance;
    }

    public AspectWrapper<T> getNext() {
        return next;
    }
}