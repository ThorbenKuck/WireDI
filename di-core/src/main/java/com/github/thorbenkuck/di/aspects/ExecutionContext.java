package com.github.thorbenkuck.di.aspects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TODO: Interface for the using aspect methods
 * @param <T>
 */
public class ExecutionContext<T extends Annotation> {

    @NotNull
    private final AspectWrapper<T> rootAspect;
    private AspectWrapper<T> currentAspect;
    @Nullable
    private final T annotation;
    @Nullable
    private final Function<ExecutionContext<T>, Object> rootMethod;
    @Nullable
    private final Map<String, Object> arguments = new HashMap<>();
    private final ExecutionContext<?> then;

    public ExecutionContext(
            @NotNull AspectWrapper<T> aspect,
            @Nullable T annotation,
            @NotNull Function<ExecutionContext<T>, Object> rootMethod
    ) {
        this.rootAspect = aspect;
        this.annotation = annotation;
        this.rootMethod = rootMethod;
        this.then = null;
    }

    public ExecutionContext(
            @NotNull AspectWrapper<T> aspect,
            @Nullable T annotation,
            @NotNull ExecutionContext<?> then
    ) {
        this.rootAspect = aspect;
        this.annotation = annotation;
        this.then = then;
        this.rootMethod = null;
    }

    void clear() {
        arguments.clear();
        if(then != null) {
            then.clear();
        }
    }

    Object run() {
        this.currentAspect = rootAspect;
        return invokeCurrentAspect();
    }

    public Object proceed() {
        AspectWrapper<T> pre = currentAspect.getNext();
        if(pre != null) {
            currentAspect = pre;
            return invokeCurrentAspect();
        } else {
            if(then != null) {
                then.arguments.putAll(this.arguments);
                return then.run();
            } else if(rootMethod != null) {
                return rootMethod.apply(this);
            } else {
                throw new IllegalStateException("There is neither a root method, nor a next ExecutionContext set. This should never happen!");
            }
        }
    }

    private Object invokeCurrentAspect() {
        return currentAspect.getRootAspect().process(this);
    }

    public Optional<Object> getArgument(String name) {
        return Optional.ofNullable(arguments.get(name));
    }

    public Object requireArgument(String name) {
        return getArgument(name).orElseThrow(() -> new IllegalArgumentException("Unknown parameter with name " + name));
    }

    public <S> S requireArgumentAs(String name, Class<S> type) {
        Object argument = requireArgument(name);
        if(!type.isInstance(argument)) {
            throw new IllegalArgumentException("The argument " + name + " was expected to be a " + type.getName() + " but actually is " + argument.getClass().getName() + ". Actual instance: " + argument);
        }

        return type.cast(argument);
    }

    public void setArgument(String name, Object value) {
        this.arguments.put(name, value);
    }

    public List<Argument> listArguments() {
        return arguments.keySet()
                .stream()
                .map(key -> new Argument(key, arguments.get(key)))
                .collect(Collectors.toList());
    }

    public Optional<T> getAnnotation() {
        return Optional.ofNullable(annotation);
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments.clear();
        this.arguments.putAll(arguments);
    }

    public static class Argument {

        private final String name;
        private final Object instance;

        public Argument(String name, Object instance) {
            this.name = name;
            this.instance = instance;
        }

        public String getName() {
            return name;
        }

        public Object getInstance() {
            return instance;
        }
    }
}
