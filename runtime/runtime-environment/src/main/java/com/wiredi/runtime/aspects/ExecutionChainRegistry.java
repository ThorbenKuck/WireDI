package com.wiredi.runtime.aspects;

import com.wiredi.logging.Logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This registry holds and maintains {@link ExecutionChain} elements.
 * <p>
 * It is preferred to use this registry instead of manually constructing {@link ExecutionChain} elements, as this class
 * is designed to allow later modifications of such a chain.
 * This registry is also supposed to be used if the aspect is created by manipulating an object using a javac plugin.
 *
 * @see ExecutionChain
 * @see AspectHandler
 */
public class ExecutionChainRegistry {

    private static final Logging logger = Logging.getInstance(ExecutionChainRegistry.class);
    private final Collection<AspectHandler> aspectHandlerList;
    private final Map<RootMethod, ExecutionChain> executionChains = new ConcurrentHashMap<>();

    public ExecutionChainRegistry(Collection<AspectHandler> aspectHandlerList) {
        this.aspectHandlerList = new ArrayList<>(aspectHandlerList);
    }

    /**
     * Returns the internally maintained {@link ExecutionChain} for the {@link RootMethod}, or creates a new one if
     * non already exists.
     *
     * @param rootMethod the Method identifier for the {@link ExecutionChain}
     * @return the existing {@link ExecutionChain}, or a new one if non exists.-
     */
    public ExecutionChain getExecutionChain(RootMethod rootMethod) {
        return executionChains.computeIfAbsent(rootMethod, this::newChain);
    }

    /**
     * Creates a new chain instance.
     *
     * @param rootMethod the root method which identifies the {@link ExecutionChain}
     * @return a new {@link ExecutionChain} with all currently known {@link AspectHandler}
     */
    private ExecutionChain newChain(RootMethod rootMethod) {
        return ExecutionChain.builder(rootMethod)
                .withProcessors(aspectHandlerList)
                .build();
    }
}
