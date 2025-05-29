package com.wiredi.test;

import com.wiredi.annotations.Wire;
import com.wiredi.annotations.aspects.Aspect;
import com.wiredi.runtime.aspects.ExecutionContext;

import java.util.concurrent.atomic.AtomicInteger;

@Wire
public class CountInvocationsAspect {

    private final AtomicInteger counts = new AtomicInteger(0);

    public int invocations() {
        return counts.get();
    }

    public CountInvocationsAspect() {
        System.out.print("");
    }

    @Aspect(around = CountInvocations.class)
    // TODO: Allow anything assignable to the actual method
    public Object incrementCount(ExecutionContext context) {
        counts.incrementAndGet();
        return context.proceed();
    }
}
