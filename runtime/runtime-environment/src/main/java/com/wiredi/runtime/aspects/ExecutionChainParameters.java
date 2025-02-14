package com.wiredi.runtime.aspects;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.wiredi.runtime.lang.Preconditions.isNotNull;

public class ExecutionChainParameters {

    private final Map<String, Object> parameters;

    public ExecutionChainParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Set<String> keySet() {
        return parameters.keySet();
    }

    private Object getParam(String name) {
        return parameters.get(name);
    }

    public void put(String name, Object value) {
        parameters.put(name, value);
    }

    public <T> Optional<T> get(String name) {
        return Optional.ofNullable(getParam(name)).map(it -> (T) it);
    }

    @NotNull
    public <T> T require(String name) {
        return (T) isNotNull(getParam(name), () -> "No parameter with the name " + name + " set");
    }
}
