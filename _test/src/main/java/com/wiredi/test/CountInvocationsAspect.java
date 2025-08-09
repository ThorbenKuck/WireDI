package com.wiredi.test;

import com.wiredi.annotations.Wire;
import com.wiredi.annotations.aspects.Aspect;
import com.wiredi.logging.Logging;
import com.wiredi.runtime.aspects.ExecutionContext;

import java.util.concurrent.atomic.AtomicInteger;

@Wire(proxy = false)
public class CountInvocationsAspect {

    private static final Logging logger = Logging.getInstance(CountInvocationsAspect.class);
    private final AtomicInteger counts = new AtomicInteger(0);

    public void reset() {
        counts.set(0);
    }

    public int invocations() {
        return counts.get();
    }

    public CountInvocationsAspect() {
        System.out.print("");
    }

    @Aspect(around = CountInvocations.class)
    // TODO: Allow anything assignable to the actual method
    public Object incrementCount(ExecutionContext context) {
        logger.debug("Incrementing invocation count on " + this);
        counts.incrementAndGet();
        return context.proceed();
    }
}
