package com.wiredi.runtime.aspects;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.async.DataAccess;
import com.wiredi.runtime.domain.Eager;

import java.util.ArrayList;
import java.util.List;
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
public class ExecutionChainRegistry implements Eager {

    private static final Logging logger = Logging.getInstance(ExecutionChainRegistry.class);
    private final DataAccess DATAACCESS = new DataAccess();
    private final List<AspectHandler> aspectHandlerList = new ArrayList<>();
    private final Map<RootMethod, ExecutionChain> executionChains = new ConcurrentHashMap<>();

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
     * Updates internally maintained {@link AspectHandler} instances.
     * <p>
     * The handlers are passed to all internally maintained {@link ExecutionChain}.
     * These {@link AspectHandler} should respect the {@link AspectHandler#appliesTo(RootMethod)} method.
     *
     * @param newHandlers the new handlers to apply to all execution chains
     */
    public void setAspectHandlers(List<AspectHandler> newHandlers) {
        DATAACCESS.write(() -> unsafeSetAspectHandlers(newHandlers));
    }

    /**
     * Inherited from eager, used to set up aspect handlers available in the {@link WireRepository}
     *
     * @param wireRepository the WireRepository the current bean is instantiated at.
     */
    @Override
    public void setup(WireRepository wireRepository) {
        DATAACCESS.write(() -> unsafeSetAspectHandlers(aspectHandlerList));
    }

    /**
     * Creates a new chain instance.
     *
     * @param rootMethod the root method which identifies the {@link ExecutionChain}
     * @return a new {@link ExecutionChain} with all currently known {@link AspectHandler}
     */
    private ExecutionChain newChain(RootMethod rootMethod) {
        return ExecutionChain.newInstance(rootMethod)
                .withProcessors(aspectHandlerList)
                .build();
    }

    /**
     * Sets all {@link AspectHandler} instances in all {@link ExecutionChain}, without any locks
     *
     * @param newHandlers the new handlers to set in the {@link ExecutionChain executionChains}
     */
    private void unsafeSetAspectHandlers(List<AspectHandler> newHandlers) {
        if (!aspectHandlerList.isEmpty()) {
            logger.info(() -> "Clearing " + aspectHandlerList.size() + " aspect handlers");
            aspectHandlerList.clear();
        }

        aspectHandlerList.addAll(newHandlers);
        executionChains.forEach((rootMethod, chain) -> chain.setHandlers(newHandlers));
    }
}
