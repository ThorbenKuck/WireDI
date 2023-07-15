package com.wiredi.aspects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO: Interface for the using aspect methods
 *
 * @param <T>
 */
public class ExecutionContext<T extends Annotation> {

    @Nullable
    private final T annotation;

    @NotNull
    private final ExecutionChainParameters parameters;

    @Nullable
    private ExecutionChainLink next = null;

    public ExecutionContext(
            @Nullable T annotation,
            @NotNull ExecutionChainParameters parameters
    ) {
        this.annotation = annotation;
        this.parameters = parameters;
    }

    public ExecutionContext(
            @Nullable T annotation
    ) {
        this(annotation, new ExecutionChainParameters());
    }

    public ExecutionContext() {
        this(null);
    }

    public <S extends Annotation> ExecutionContext<S> prepend(S annotation, ExecutionChainLink nextElement) {
        ExecutionContext<S> executionContext = new ExecutionContext<>(annotation, parameters);
        executionContext.next = nextElement;
        return executionContext;
    }

    public void clear() {
        parameters.clear();
    }

    public ExecutionChainParameters parameters() {
        return parameters;
    }

    @Nullable
    public <S> S proceed() {
        if (next == null) {
            throw new IllegalStateException("Proceed was called on the first chain element, which is not allowed");
        }
        return next.execute();
    }

    @NotNull
    public Optional<Object> getParameter(@NotNull final String name) {
        return parameters.get(name);
    }

    @NotNull
    public <S> S requireParameter(@NotNull final String name) {
        return (S) getParameter(name).orElseThrow(() -> new IllegalArgumentException("Unknown parameter with name " + name));
    }

    public void setParameter(
            @NotNull final String name,
            @NotNull final Object value
    ) {
        this.parameters.put(name, value);
    }

    @NotNull
    public List<Argument> listArguments() {
        return parameters.keySet()
                .stream()
                .map(key -> new Argument(key, parameters.get(key)))
                .collect(Collectors.toList());
    }

    @NotNull
    public T getAnnotation() {
        return annotation;
    }

    public void setParameters(@NotNull Map<String, Object> parameters) {
        this.parameters.clear();
        this.parameters.set(parameters);
    }

    public record Argument(@NotNull String name, @Nullable Object instance) {
    }
}
