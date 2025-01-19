package com.wiredi.logging;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoggingAccessor {

    private final Map<String, Logging> instance = new ConcurrentHashMap<>();

    public Logging get(Class<?> type) {
        return get(type.getName());
    }

    public Logging get(String name) {
        return instance.computeIfAbsent(name, Logging::getInstance);
    }

    public void disable(Class<?> type) {
        disable(type.getName());
    }

    public void disable(String name) {
        instance.computeIfPresent(name, (n, logging) -> logging.setEnabled(false));
    }

    public void enable(Class<?> type) {
        enable(type.getName());
    }

    public void enable(String name) {
        instance.computeIfPresent(name, (n, logging) -> logging.setEnabled(true));
    }
}
