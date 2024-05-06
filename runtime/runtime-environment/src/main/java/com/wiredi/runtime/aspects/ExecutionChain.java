package com.wiredi.runtime.aspects;

import com.wiredi.runtime.aspects.links.RootMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * An ExecutionChain is a chain of responsibility pattern implementation.
 * <p>
 * The chain has a tail and a head. The tail will always be the {@link #rootMethod}, which is
 * the original and concrete method you want to delegate to.
 * <p>
 * At the beginning of a chains constructions, the head is the same as the tail. Upon calling
 * {@link #prepend(AspectHandler)}, a new head ist constructed and the existing head
 * is replaced by the new head. In the relativeEnd, the last added {@link AspectHandler} will be the
 * head of the execution chain. The tail will always be root method.
 * <p>
 * A constructed {@link ExecutionChain} will hold the head/tail information, but for execution
 * the {@link #execute()} method must be called. This will construct a new {@link ExecutionStage}
 * in which the context of a concrete execution can be modified.
 * <p>
 * Though this class is meant to be used for aspect executions, it can be used standalone.
 *
 * @see RootMethod
 * @see ExecutionChainLink
 * @see AspectHandler
 * @see ExecutionContext
 */
public class ExecutionChain {

    /**
     * The original method, also known as the tail.
     */
    @NotNull
    private final RootMethod rootMethod;

    /**
     * The current head of the chain. At the beginning this will be the {@link #rootMethod} and this
     * will be replaced by the {@link #prepend(AspectHandler)} method
     */
    @NotNull
    private ExecutionChainLink head;

    public ExecutionChain(
            @NotNull RootMethod rootFunction
    ) {
        this.rootMethod = rootFunction;
        this.head = rootFunction;
    }

    @NotNull
    public static Builder newInstance(@NotNull RootMethod rootMethod) {
        return new Builder(rootMethod);
    }

    @NotNull
    public ExecutionChain prepend(@NotNull AspectHandler handler) {
        this.head = head.prepend(handler);
        return this;
    }

    @NotNull
    public RootMethod rootMethod() {
        return rootMethod;
    }

    @NotNull
    public ExecutionChainLink tail() {
        return rootMethod;
    }

    @NotNull
    public ExecutionChainLink head() {
        return head;
    }

    @Nullable
    public <S> S execute(@NotNull Map<String, Object> parameters) {
        Object result = doExecute(parameters);
        if (result == null) {
            return null;
        } else {
            return (S) result;
        }
    }

    @Nullable
    public <S> S execute(@NotNull Map<String, Object> parameters, @NotNull Class<S> type) {
        Object result = doExecute(parameters);
        if (result == null) {
            return null;
        } else {
            return type.cast(result);
        }
    }

    @NotNull
    public ExecutionStage execute() {
        return new ExecutionStage();
    }

    @Nullable
    private Object doExecute(@NotNull Map<String, Object> parameters) {
        try {
            rootMethod.parameters().set(parameters);
            return head.executeRaw();
        } finally {
            rootMethod.parameters().clear();
        }
    }

    public static class Builder {

        @NotNull
        private final RootMethod rootMethod;
        @NotNull
        private final Queue<ComponentBuilder<? extends Annotation>> prepends = new LinkedBlockingDeque<>();
        private boolean distinct = false;

        public Builder(@NotNull RootMethod rootMethod) {
            this.rootMethod = rootMethod;
        }

        @NotNull
        public Builder withProcessor(@NotNull AspectHandler handler) {
            if (handler.appliesTo(rootMethod)) {
                prepends.add(new ComponentBuilder<>(handler));
            }
            return this;
        }

        @NotNull
        public Builder withProcessors(@NotNull List<AspectHandler> handlers) {
            for (AspectHandler handler : handlers) {
                if (handler.appliesTo(rootMethod)) {
                    prepends.add(new ComponentBuilder<>(handler));
                }
            }
            return this;
        }

        /**
         * Whether the builder should filter out duplicate handlers.
         * <p>
         * If true, the builder will only use handlers once, even if added multiple times.
         * If false, the builder will not care about duplicated handlers and will allow duplicates.
         *
         * @param distinct whether duplicates should be filtered or not
         * @return this
         */
        @NotNull
        public Builder distinct(boolean distinct) {
            this.distinct = distinct;
            return this;
        }

        @NotNull
        public ExecutionChain build() {
            if(distinct) {
                return buildDistinct();
            } else {
                return buildIndistinct();
            }
        }

        private ExecutionChain buildDistinct() {
            final ExecutionChain executionChain = new ExecutionChain(rootMethod);
            final HashSet<AspectHandler> handlers = new HashSet<>();

            while (prepends.peek() != null) {
                ComponentBuilder<? extends Annotation> current = prepends.poll();
                if (!handlers.contains(current.function)) {
                    prepend(executionChain, current);
                    handlers.add(current.function);
                }
            }

            return executionChain;
        }

        private ExecutionChain buildIndistinct() {
            final ExecutionChain executionChain = new ExecutionChain(rootMethod);

            while (prepends.peek() != null) {
                prepend(executionChain, prepends.poll());
            }

            return executionChain;
        }

        private <T extends Annotation> void prepend(@NotNull ExecutionChain executionChain, @NotNull ComponentBuilder<T> componentBuilder) {
            executionChain.prepend(componentBuilder.function);
        }

        private record ComponentBuilder<T extends Annotation>(@NotNull AspectHandler function) {
        }
    }

    public class ExecutionStage {
        @NotNull
        private final Map<String, Object> content = new HashMap<>();

        @NotNull
        public ExecutionStage withParameter(@NotNull String name, @Nullable Object value) {
            content.put(name, value);
            return this;
        }

        @Nullable
        public <S> S andReturn() {
            return ExecutionChain.this.execute(content);
        }
    }
}
