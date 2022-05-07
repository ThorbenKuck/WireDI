package com.github.thorbenkuck.di.aspects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class AspectExecutionContext {

    @NotNull
    private final AtomicReference<ExecutionContext<?>> executionContextAtomicReference = new AtomicReference<>();

    @NotNull
    private final Map<String, Object> arguments = new HashMap<>();

    @NotNull
    private final AspectRepository aspectRepository;

    @NotNull
    private final Function<ExecutionContext<?>, Object> realMethod;

    public AspectExecutionContext(
            @NotNull final AspectRepository aspectRepository,
            @NotNull final Function<ExecutionContext<?>, Object> realMethod
    ) {
        this.aspectRepository = aspectRepository;
        this.realMethod = realMethod;
    }

    @NotNull
    public <T extends Annotation> AspectExecutionContext announceInterestForAspect(
            @NotNull final Class<T> annotationType,
            @Nullable final T annotation
    ) {
        aspectRepository.access(annotationType).ifPresent(aspect -> {
            final ExecutionContext<?> methodContext = executionContextAtomicReference.get();
            final ExecutionContext<?> currentContext;
            if (methodContext == null) {
                currentContext = new ExecutionContext(aspect, annotation, realMethod);
            } else {
                currentContext = new ExecutionContext<>(aspect, annotation, methodContext);
            }
            executionContextAtomicReference.set(currentContext);
        });

        return this;
    }

    public <T> void declareArgument(@NotNull final String name, @Nullable final T t) {
        arguments.put(name, t);
    }

    public boolean noAspectsPresent() {
        return executionContextAtomicReference.get() == null;
    }

    @Nullable
    public Object run(final boolean mayBeNull) {
        final ExecutionContext<?> executionContext = executionContextAtomicReference.get();
        if(executionContext == null) {
            throw new IllegalStateException("No ExecutionContext set");
        }
        executionContext.setArguments(arguments);

        try {
            final Object result = executionContext.run();
            if (!mayBeNull && result == null) {
                throw new NullPointerException("Expect the result not to be null, but yet it is");
            }

            return result;
        } finally {
            arguments.clear();
            executionContext.clear();
        }
    }
}
