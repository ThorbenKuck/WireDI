package com.github.thorbenkuck.di.runtime;

import com.github.thorbenkuck.di.domain.ContextCallback;

import java.util.*;
import java.util.function.Consumer;

public class WireRepositoryCallbackRepository {

    private static Set<ContextCallback> GLOBAL_CONTEXT_CALLBACKS = new HashSet<>();
    private final Set<ContextCallback> callbackList = new HashSet<>(GLOBAL_CONTEXT_CALLBACKS);

    static {
        ServiceLoader.load(ContextCallback.class)
                .forEach(GLOBAL_CONTEXT_CALLBACKS::add);
    }

    public static void addGlobally(ContextCallback contextCallback) {
        GLOBAL_CONTEXT_CALLBACKS.add(contextCallback);
    }

    public void add(ContextCallback contextCallback) {
        callbackList.add(contextCallback);
    }

    public void forEach(Consumer<ContextCallback> contextCallbackConsumer) {
        callbackList.forEach(contextCallbackConsumer);
    }
}
