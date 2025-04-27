package com.wiredi.runtime.aspects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An ExecutionChain is a chain of responsibility pattern implementation.
 * <p>
 * The chain has a tail and a head. The tail will always be the {@link #rootMethod}, which is
 * the original and concrete method you want to delegate to.
 * <p>
 * At the beginning of a chains constructions, the effective head is the same as the effective tail.
 * Upon calling {@link #prepend(AspectHandler)}, a new head ist constructed and the existing head is replaced by the new head.
 * In the relativeEnd, the last added {@link AspectHandler} will be the head of the execution chain.
 * The tail will always be root method.
 * <p>
 * A constructed {@link ExecutionChain} will hold the head/tail information, but for execution
 * the {@link #execute()} method must be called. This will construct a new {@link ExecutionStage}
 * in which the context of a concrete execution can be modified.
 * <p>
 * Though this class is meant to be used for aspect executions, it can be used standalone.
 *
 * @see RootMethod
 * @see AspectHandler
 * @see ExecutionContext
 * @see ExecutionChainRegistry
 */
public class ExecutionChain {

    /**
     * The original method which will be invoked after the chain elements in the {@link #queue}
     */
    @NotNull
    private final RootMethod rootMethod;

    @NotNull
    private final Deque<AspectHandler> queue = new LinkedBlockingDeque<>();

    @NotNull
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public ExecutionChain(
            @NotNull RootMethod rootFunction
    ) {
        this.rootMethod = rootFunction;
    }

    @NotNull
    public static Builder builder(@NotNull RootMethod rootMethod) {
        return new Builder(rootMethod);
    }

    @NotNull
    public ExecutionChain prepend(@NotNull AspectHandler handler) {
        write(() -> this.queue.addFirst(handler));
        return this;
    }

    @NotNull
    public ExecutionChain append(@NotNull AspectHandler handler) {
        write(() -> this.queue.addLast(handler));
        return this;
    }

    public ExecutionChain setHandlers(@NotNull Collection<AspectHandler> handlers) {
        List<AspectHandler> applicableHandlers = handlers.stream()
                .filter(it -> it.appliesTo(this.rootMethod))
                .toList();

        write(() -> {
            this.queue.clear();
            this.queue.addAll(applicableHandlers);
        });
        return this;
    }

    @NotNull
    public RootMethod rootMethod() {
        return rootMethod;
    }

    @NotNull
    public AspectHandler tail() {
        return this.queue.getLast();
    }

    @NotNull
    public AspectHandler head() {
        return this.queue.getFirst();
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
        ArrayDeque<AspectHandler> handlers;
        try {
            lock.readLock().lock();
            handlers = new ArrayDeque<>(this.queue.size() + 1);
            handlers.addAll(this.queue);
            handlers.addLast(this.rootMethod);
        } finally {
            lock.readLock().unlock();
        }

        ExecutionContext executionContext = new ExecutionContext(rootMethod, new ExecutionChainParameters(parameters), handlers);
        try {
            return executionContext.proceed();
        } finally {
            executionContext.clear();
        }
    }

    private void write(Runnable runnable) {
        lock.writeLock().lock();
        try {
            runnable.run();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static class Builder {

        @NotNull
        private final RootMethod rootMethod;
        @NotNull
        private final Deque<AspectHandler> handlers = new LinkedBlockingDeque<>();
        private boolean distinct = false;

        public Builder(@NotNull RootMethod rootMethod) {
            this.rootMethod = rootMethod;
        }

        @NotNull
        public Builder withProcessor(@NotNull AspectHandler handler) {
            if (handler.appliesTo(rootMethod)) {
                handlers.add(handler);
            }
            return this;
        }

        @NotNull
        public Builder withProcessors(@NotNull List<AspectHandler> handlers) {
            for (AspectHandler handler : handlers) {
                if (handler.appliesTo(rootMethod)) {
                    this.handlers.add(handler);
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
            final LinkedBlockingDeque<AspectHandler> aspectHandlers = new LinkedBlockingDeque<>();

            if (distinct) {
                aspectHandlers.addAll(new HashSet<>(this.handlers));
            } else {
                aspectHandlers.addAll(this.handlers);
            }

            return new ExecutionChain(rootMethod).setHandlers(aspectHandlers);
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
