package com.wiredi.runtime.aspects;

import com.wiredi.runtime.aspects.links.RootMethod;
import com.wiredi.runtime.domain.AnnotationMetaData;
import com.wiredi.runtime.properties.ThreadLocalTypedProperties;
import com.wiredi.runtime.properties.TypedProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.wiredi.runtime.lang.Preconditions.isNotNull;

/**
 * TODO: Interface for the using aspect methods
 */
public class ExecutionContext {

    @NotNull
    private final RootMethod rootMethod;

    @NotNull
    private final ExecutionChainParameters parameters;

    @NotNull
    private final ThreadLocalTypedProperties typedProperties = new ThreadLocalTypedProperties();

    @Nullable
    private ExecutionChainLink next = null;

    public ExecutionContext(
            @NotNull RootMethod rootMethod,
            @NotNull ExecutionChainParameters parameters
    ) {
        this.rootMethod = rootMethod;
        this.parameters = parameters;
    }

    public ExecutionContext(@NotNull RootMethod rootMethod) {
        this(rootMethod, new ExecutionChainParameters());
    }

    @NotNull
    public ExecutionContext prepend(ExecutionChainLink nextElement) {
        ExecutionContext executionContext = new ExecutionContext(rootMethod, parameters);
        executionContext.next = nextElement;
        return executionContext;
    }

    public void clear() {
        parameters.clear();
    }

    @NotNull
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
    public RootMethod getRootMethod() {
        return rootMethod;
    }

    public Optional<AnnotationMetaData> findAnnotation(Predicate<AnnotationMetaData> predicate) {
        return rootMethod.findAnnotation(predicate);
    }

    public Optional<AnnotationMetaData> findAnnotation(Class<? extends Annotation> annotation) {
        return rootMethod.findAnnotation(annotation);
    }

    public AnnotationMetaData getAnnotation(Class<? extends Annotation> annotation) {
        return rootMethod.getAnnotation(annotation);
    }

    public void setParameters(@NotNull Map<String, Object> parameters) {
        this.parameters.clear();
        this.parameters.set(parameters);
    }

    @NotNull
    public TypedProperties properties() {
        return typedProperties.get();
    }

    public record Argument(@NotNull String name, @Nullable Object instance) {
    }
}
