package com.wiredi.test;

import com.wiredi.annotations.Wire;
import com.wiredi.annotations.aspects.Aspect;
import com.wiredi.aspects.ExecutionChain;

import java.util.stream.Collectors;

@Wire
public class TestFacet {

    @Aspect(around = PrintParameter.class)
    // TODO: Allow anything assignable to the actual method
    // TODO: Why is this executed three times?
    public Object printParametersAspect(ExecutionChain<PrintParameter> context) {
        String parameters = context.listArguments()
                .stream()
                .map(it -> it.getName() + "=" + it.getInstance())
                .collect(Collectors.joining(",", "[", "]"));
        System.out.println("Parameters" + parameters);

        return context.proceed();
    }
}
