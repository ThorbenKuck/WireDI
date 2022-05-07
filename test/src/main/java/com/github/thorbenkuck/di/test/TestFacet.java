package com.github.thorbenkuck.di.test;

import com.github.thorbenkuck.di.annotations.aspects.Aspect;
import com.github.thorbenkuck.di.annotations.aspects.Facet;
import com.github.thorbenkuck.di.aspects.ExecutionContext;

import java.util.stream.Collectors;

@Facet
public class TestFacet {

    @Aspect(around = PrintParameter.class)
    // TODO: Allow anything assignable to the actual method
    // TODO: Why is this executed three times?
    public Object printParametersAspect(ExecutionContext<PrintParameter> context) {
        String parameters = context.listArguments()
                .stream()
                .map(it -> it.getName() + "=" + it.getInstance())
                .collect(Collectors.joining(",", "[", "]"));
        System.out.println("Parameters" + parameters);

        return context.proceed();
    }
}
