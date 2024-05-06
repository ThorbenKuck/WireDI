package com.wiredi.runtime.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A TypeMap is a HashMap class, which is hard fixed to contain class keys.
 * <p>
 * Instead of directly storing class references, it uses the fully qualified class name instead.
 * This way, the class is safe against reloading classes and will not hold classes hostage from being collected
 * by the GC when the ClassLoader is dynamically reloaded or enhanced.
 * <p>
 * A TypeMap can be understood as: {@code Map<Class<?>, T>}.
 *
 * <pre><code>
 * class Example {
 *
 *     private final TypeMap&#60;String&#62; map = new TypeMap&#60;&#62;()
 *
 *     public void register(Class&#60;?&#62; type, String value) {
 *         map.put(type, value);
 *     }
 * }
 * </code></pre>
 *
 * @param <T>
 */
public class TypeMap<T> {

    protected final Map<String, T> contents;

    protected TypeMap(Map<String, T> contents) {
        this.contents = contents;
    }

    public TypeMap() {
        this(new HashMap<>());
    }

    public TypeMap(TypeMap<T> other) {
        this(new HashMap<>(other.contents));
    }

    public TypeMap(int initialCapacity) {
        this(new HashMap<>(initialCapacity));
    }

    public TypeMap(int initialCapacity, float loadFactor) {
        this(new HashMap<>(initialCapacity, loadFactor));
    }

    public int size() {
        return contents.size();
    }

    public boolean isEmpty() {
        return contents.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public boolean containsValue(T value) {
        return contents.containsValue(value);
    }
    
    public T get(Class<?> type) {
        return contents.get(type.getName());
    }
    
    public boolean containsKey(Class<?> type) {
        return contents.containsKey(type.getName());
    }

    public T getOrDefault(Class<?> type, T or) {
        return contents.getOrDefault(type.getName(), or);
    }
    
    public T put(Class<?> type, T value) {
        return contents.put(type.getName(), value);
    }
    
    public T remove(Class<?> type) {
        return contents.remove(type.getName());
    }
    
    public void putAll(TypeMap<? extends T> typeMap) {
        this.contents.putAll(typeMap.contents);
    }
    
    public void putAll(Map<? extends Class<?>, ? extends T> map) {
        map.forEach(this::put);
    }
    
    public void ifPresent(Class<?> type, Consumer<T> consumer) {
        Optional.ofNullable(get(type)).ifPresent(consumer);
    }
    
    public void ifAbsent(Class<?> type, Consumer<T> consumer) {
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
        return contents.toString();
    }

    @Override
    public int hashCode() {
        return contents.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeMap<?> that)) return false;
        return Objects.equals(contents, that.contents);
    }

}
