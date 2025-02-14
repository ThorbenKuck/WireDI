package com.wiredi.runtime.aspects;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.async.DataAccess;
import com.wiredi.runtime.domain.Eager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutionChainRegistry implements Eager {

    private static final Logging logger = Logging.getInstance(ExecutionChainRegistry.class);
    private static final ExecutionChainRegistry INSTANCE = new ExecutionChainRegistry();
    private final DataAccess DATAACCESS = new DataAccess();
    private final List<AspectHandler> aspectHandlerList = new ArrayList<>();
    private final Map<RootMethod, ExecutionChain> executionChains = new ConcurrentHashMap<>();

    public static ExecutionChainRegistry getInstance() {
        return INSTANCE;
    }

    public ExecutionChain getExecutionChain(RootMethod rootMethod) {
        return executionChains.computeIfAbsent(rootMethod, this::newChain);
    }

    public void setAspectHandlers(List<AspectHandler> newHandlers) {
        DATAACCESS.write(() -> unsafeSetAspectHandlers(newHandlers));
    }

    @Override
    public void setup(WireRepository wireRepository) {
        DATAACCESS.write(() -> unsafeSetAspectHandlers(aspectHandlerList));
    }

    private ExecutionChain newChain(RootMethod rootMethod) {
        return ExecutionChain.newInstance(rootMethod)
                .withProcessors(aspectHandlerList)
                .build();
    }

    private void unsafeSetAspectHandlers(List<AspectHandler> newHandlers) {
        if (!aspectHandlerList.isEmpty()) {
            logger.info(() -> "Clearing " + aspectHandlerList.size() + " aspect handlers");
            aspectHandlerList.clear();
        }

        aspectHandlerList.addAll(newHandlers);
        executionChains.forEach((rootMethod, chain) -> chain.setHandlers(newHandlers));
    }
}
