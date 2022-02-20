package com.github.thorbenkuck.di.aspects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class AspectExecutionContext {

    private final AtomicReference<ExecutionContext> executionContextAtomicReference = new AtomicReference();
    private final Map<String, Object> arguments = new HashMap<>();
    private final AspectRepository aspectRepository;
    private final Function<ExecutionContext, Object> realMethod;

    public AspectExecutionContext(AspectRepository aspectRepository, Function<ExecutionContext, Object> realMethod) {
        this.aspectRepository = aspectRepository;
        this.realMethod = realMethod;
    }

    public <T extends Annotation> AspectExecutionContext announceInterestForAspect(
            @NotNull Class<T> annotationType,
            @Nullable T annotation
    ) {
        aspectRepository.access(annotationType).ifPresent(aspect -> {
            ExecutionContext methodContext = executionContextAtomicReference.get();
            ExecutionContext<Override> currentContext;
            if (methodContext == null) {
                currentContext = new ExecutionContext(aspect, annotation, realMethod);
            } else {
                currentContext = new ExecutionContext(aspect, annotation, methodContext);
            }
            executionContextAtomicReference.set(currentContext);
        });

        return this;
    }

    public <T> void declareArgument(@NotNull String name, @Nullable T t) {
        arguments.put(name, t);
    }

    public boolean noAspectsPresent() {
        return executionContextAtomicReference.get() == null;
    }

    @Nullable
    public Object run(boolean mayBeNull) {
        ExecutionContext executionContext = executionContextAtomicReference.get();
        executionContext.setArguments(arguments);
        try {
            Object result = executionContext.run();
            if(!mayBeNull) {
                throw new NullPointerException("Expect the result not to be null, but yet it is");
            }

            return result;
        } finally {
            arguments.clear();
            executionContext.clear();
        }
    }
}
