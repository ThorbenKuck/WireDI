package com.wiredi.runtime.aspects;

import com.wiredi.runtime.domain.annotations.AnnotationMetadata;
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
    private final Deque<AspectHandler> aspectHandlers;

    @NotNull
    private final ThreadLocalTypedProperties typedProperties = new ThreadLocalTypedProperties();

    public ExecutionContext(
            @NotNull RootMethod rootMethod,
            @NotNull ExecutionChainParameters parameters,
            @NotNull Deque<AspectHandler> aspectHandlers
    ) {
        this.rootMethod = rootMethod;
        this.parameters = parameters;
        this.aspectHandlers = aspectHandlers;
    }

    public void clear() {
        aspectHandlers.clear();
    }

    @NotNull
    public ExecutionChainParameters parameters() {
        return parameters;
    }

    @Nullable
    public <S> S proceed() {
        if (aspectHandlers.isEmpty()) {
            throw new IllegalStateException("No further aspect handlers available. This should normally not happen, as the last element always should be the RootMethod!");
        }

        return (S) aspectHandlers.pop().process(this);
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

    public Optional<AnnotationMetadata> findAnnotation(Predicate<AnnotationMetadata> predicate) {
        return rootMethod.findAnnotation(predicate);
    }

    public Optional<AnnotationMetadata> findAnnotation(Class<? extends Annotation> annotation) {
        return rootMethod.findAnnotation(annotation);
    }

    public AnnotationMetadata getAnnotation(Class<? extends Annotation> annotation) {
        return rootMethod.getAnnotation(annotation);
    }

    @NotNull
    public TypedProperties properties() {
        return typedProperties.get();
    }

    public record Argument(@NotNull String name, @Nullable Object instance) {
    }
}
