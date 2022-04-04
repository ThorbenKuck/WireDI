package com.github.thorbenkuck.di.processor.extensions;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ExtensionContext {

    private final List<Class<? extends Annotation>> registeredAnnotations = new ArrayList<>();
    private boolean allInterrestsDeclared = false;

    public void announceInterrest(Class<? extends Annotation>... type) {
        if(allInterrestsDeclared)  {
            throw new IllegalStateException("The interest declaration time is already over");
        }
        registeredAnnotations.addAll(Arrays.asList(type));
    }

    public void allInterrestsHaveBeenDeclared() {
        allInterrestsDeclared = true;
    }

    public Stream<Class<? extends Annotation>> stream() {
        return registeredAnnotations.stream();
    }
}
