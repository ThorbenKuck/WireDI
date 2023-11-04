package com.wiredi.lang.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TypeMap<T> {

    private final Map<String, T> contents = new HashMap<>();

    public int size() {
        return contents.size();
    }

    public boolean isEmpty() {
        return contents.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public boolean containsKey(Class<?> type) {
        return contents.containsKey(type.getName());
    }

    public boolean containsValue(T value) {
        return contents.containsValue(value);
    }

    public T get(Class<?> type) {
        return contents.get(type.getName());
    }

    public T getOrDefault(Class<?> type, T or) {
        return contents.getOrDefault(type.getName(), or);
    }

    public T put(Class<?> type, T value) {
        return contents.put(type.getName(), value);
    }

    public void ifPresent(Class<?> type, Consumer<T> consumer) {
        Optional.ofNullable(get(type)).ifPresent(consumer);
    }

    public T computeIfAbsent(Class<?> type, Supplier<T> supplier) {
        return contents.computeIfAbsent(type.getName(), (t) -> supplier.get());
    }

    public T computeIfPresent(Class<?> type, Function<T, T> function) {
        return contents.computeIfPresent(type.getName(), (key, existing) -> function.apply(existing));
    }

    public void clear() {
        contents.clear();
    }

    @Override
    public String toString() {
        return "TypeMap{" +
                "contents=" + contents +
                '}';
    }
}
