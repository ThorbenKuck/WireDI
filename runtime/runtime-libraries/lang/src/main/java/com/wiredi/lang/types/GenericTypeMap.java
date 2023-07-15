package com.wiredi.lang.types;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class GenericTypeMap<C, T> {

    private final Map<String, T> root = new HashMap<>();

    public int size() {
        return root.size();
    }

    public boolean isEmpty() {
        return root.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public boolean containsKey(Class<? extends C> type) {
        return root.containsKey(type.getName());
    }

    public boolean containsValue(T value) {
        return root.containsValue(value);
    }

    public T get(Class<? extends C> type) {
        return root.get(type.getName());
    }

    public T getOrDefault(Class<? extends C> type, T or) {
        return root.getOrDefault(type, or);
    }

    public T put(Class<? extends C> type, T value) {
        return root.put(type.getName(), value);
    }

    public void ifPresent(Class<? extends C> type, Consumer<T> consumer) {
        Optional.ofNullable(get(type)).ifPresent(consumer);
    }

    public T computeIfAbsent(Class<? extends C> type, Supplier<T> supplier) {
        return root.computeIfAbsent(type.getName(), (t) -> supplier.get());
    }

    public T computeIfPresent(Class<? extends C> type, Function<T, T> function) {
        return root.computeIfPresent(type.getName(), (key, existing) -> function.apply(existing));
    }

    public void clear() {
        root.clear();
    }
}
