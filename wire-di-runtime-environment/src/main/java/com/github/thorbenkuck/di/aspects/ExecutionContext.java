package com.github.thorbenkuck.di.aspects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TODO: Interface for the using aspect methods
 *
 * @param <T>
 */
public class ExecutionContext<T extends Annotation> {

    @NotNull
    private final AspectWrapper<T> rootAspect;
    @Nullable
    private AspectWrapper<T> currentAspectPointer;
    @Nullable
    private final T annotation;
    @NotNull
    private final Map<String, Object> arguments = new HashMap<>();
    @Nullable
    private final Function<ExecutionContext<T>, Object> rootMethod;
    @Nullable
    private final ExecutionContext<?> then;

    @Nullable
    private Object lastReturnValue = null;

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
            @NotNull final AspectWrapper<T> aspect,
            @Nullable final T annotation,
            @NotNull final ExecutionContext<?> then
    ) {
        this.rootAspect = aspect;
        this.annotation = annotation;
        this.then = then;
        this.rootMethod = null;
    }

    void clear() {
        arguments.clear();
        if (then != null) {
            then.clear();
        }
    }

    @Nullable
    Object run() {
        this.currentAspectPointer = rootAspect;
        return invokeCurrentAspect();
    }

    @Nullable
    public Object proceed() {
        if (currentAspectPointer == null) {
            throw new IllegalStateException("No aspect pointer registered");
        }
        final AspectWrapper<T> followup = currentAspectPointer.getNext();
        if (followup != null) {
            currentAspectPointer = followup;
            return invokeCurrentAspect();
        } else {
            if (then != null) {
                then.arguments.putAll(this.arguments);
                then.lastReturnValue = lastReturnValue;
                return then.run();
            } else if (rootMethod != null) {
                return rootMethod.apply(this);
            } else {
                throw new IllegalStateException("There is neither a root method, nor a next ExecutionContext set. This should never happen!");
            }
        }
    }

    @Nullable
    private Object invokeCurrentAspect() {
        if (currentAspectPointer == null) {
            throw new IllegalStateException("No aspect pointer registered");
        }
        return currentAspectPointer.getRootAspect().process(this);
    }

    @NotNull
    public Optional<Object> getArgument(@NotNull final String name) {
        return Optional.ofNullable(arguments.get(name));
    }

    @NotNull
    public Object requireArgument(@NotNull final String name) {
        return getArgument(name).orElseThrow(() -> new IllegalArgumentException("Unknown parameter with name " + name));
    }

    @NotNull
    public <S> S requireArgumentAs(
            @NotNull final String name,
            @NotNull final Class<S> type
    ) {
        final Object argument = requireArgument(name);
        if (!type.isInstance(argument)) {
            throw new IllegalArgumentException("The argument " + name + " was expected to be a " + type.getName() + " but actually is " + argument.getClass().getName() + ". Actual instance: " + argument);
        }

        return type.cast(argument);
    }

    public void setArgument(
            @NotNull final String name,
            @NotNull final Object value
    ) {
        this.arguments.put(name, value);
    }

    @NotNull
    public List<Argument> listArguments() {
        return arguments.keySet()
                .stream()
                .map(key -> new Argument(key, arguments.get(key)))
                .collect(Collectors.toList());
    }

    @NotNull
    public Optional<T> getAnnotation() {
        return Optional.ofNullable(annotation);
    }

    public void setArguments(@NotNull Map<String, Object> arguments) {
        this.arguments.clear();
        this.arguments.putAll(arguments);
    }

    @Nullable
    public Object getLastReturnValue() {
        return lastReturnValue;
    }

    @NotNull
    public Object requireLastReturnValue() {
        return Objects.requireNonNull(getLastReturnValue(), "Last return value is not present");
    }

    @Nullable
    public <S> S getLastReturnValueAs(Class<S> type) {
        if(lastReturnValue == null) {
            return null;
        }
        if (!type.isInstance(lastReturnValue)) {
            throw new IllegalArgumentException("The current last return was expected to be a " + type.getName() + " but actually is " + lastReturnValue.getClass().getName() + ". Actual instance: " + lastReturnValue);
        }

        return type.cast(lastReturnValue);
    }

    @NotNull
    public <S> S requireLastReturnValueAs(Class<S> type) {
        return Objects.requireNonNull(getLastReturnValueAs(type), "Last return value is not present");
    }

    public static class Argument {

        @NotNull
        private final String name;
        @Nullable
        private final Object instance;

        public Argument(
                @NotNull final String name,
                @Nullable final Object instance
        ) {
            this.name = name;
            this.instance = instance;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @Nullable
        public Object getInstance() {
            return instance;
        }
    }
}
