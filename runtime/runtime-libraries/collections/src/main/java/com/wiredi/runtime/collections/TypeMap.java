package com.wiredi.runtime.collections;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A TypeMap is a specialized map keyed by Java types, optimized for classloader-safe lookups.
 * Instead of storing {@link Class} instances directly as keys, it uses their fully qualified class names.
 * This avoids preventing classes from being garbage collected when classloaders are reloaded or enhanced.
 * <p>
 * Conceptually, it behaves like {@code Map<Class<?>, T>} where all operations accept {@link Class} keys,
 * while internally delegating to a {@code Map<String, T>}.
 * <p>
 * Typical usage stores values by their type to support fast, type-directed lookups without keeping strong
 * references to {@link Class} objects:
 * <pre>{@code
 * class Registry {
 *   private final TypeMap<Object> byType = new TypeMap<>();
 *
 *   public <T> void register(Class<T> type, T instance) {
 *     byType.put(type, instance);
 *   }
 *
 *   public <T> T get(Class<T> type) {
 *     return type.cast(byType.get(type));
 *   }
 * }
 *
 * Registry r = new Registry();
 * r.register(Number.class, 42);
 * Integer i = r.get(Integer.class);        // null
 * Number n = r.get(Number.class);          // 42
 * }
 * </pre>
 * <p>
 * Thread-safety: This class is not thread-safe by default. Use {@link ConcurrentTypeMap} if you need
 * concurrent access.
 *
 * @param <T> the value type stored in the map
 */
public class TypeMap<T> {

    /** The backing map using fully qualified class names as keys. */
    protected final Map<String, T> contents;

    /**
     * Creates a TypeMap with the provided backing map. Subclasses use this to provide specialized maps.
     * @param contents a mutable map keyed by fully qualified class names
     */
    protected TypeMap(Map<String, T> contents) {
        this.contents = contents;
    }

    /**
     * Creates an empty TypeMap backed by a {@link HashMap}.
     */
    public TypeMap() {
        this(new HashMap<>());
    }

    /**
     * Copy-constructor that clones the content of another TypeMap into a new {@link HashMap}.
     * @param other the map to copy
     */
    public TypeMap(TypeMap<T> other) {
        this(new HashMap<>(other.contents));
    }

    /**
     * Creates an empty TypeMap with the given initial capacity.
     */
    public TypeMap(int initialCapacity) {
        this(new HashMap<>(initialCapacity));
    }

    /**
     * Creates an empty TypeMap with the given initial capacity and load factor.
     */
    public TypeMap(int initialCapacity, float loadFactor) {
        this(new HashMap<>(initialCapacity, loadFactor));
    }

    /**
     * Returns a live view of the values stored in this map.
     */
    public Collection<T> values() {
        return contents.values();
    }

    /**
     * Returns a view of the underlying string keys (fully qualified class names).
     */
    public Collection<String> keys() {
        return contents.keySet();
    }

    /** Returns the number of entries. */
    public int size() {
        return contents.size();
    }

    /** Returns whether this map contains no entries. */
    public boolean isEmpty() {
        return contents.isEmpty();
    }

    /** Returns whether this map contains at least one entry. */
    public boolean isNotEmpty() {
        return !isEmpty();
    }

    /** Returns whether the given value is contained at least once. */
    public boolean containsValue(T value) {
        return contents.containsValue(value);
    }

    /**
     * Returns the value associated with the given type or {@code null} if none.
     * @param type the type key
     */
    public T get(Class<?> type) {
        return contents.get(type.getName());
    }

    /**
     * Returns the value associated with the given fully qualified class name or {@code null}.
     */
    public T get(String className) {
        return contents.get(className);
    }

    /** Returns whether there is a mapping for the given type. */
    public boolean containsKey(Class<?> type) {
        return contents.containsKey(type.getName());
    }

    /**
     * Returns the value associated with the given type or the provided default if absent.
     */
    public T getOrDefault(Class<?> type, T or) {
        return contents.getOrDefault(type.getName(), or);
    }

    /**
     * Associates the given value with the given type.
     * @return the previous value or {@code null}
     */
    public T put(Class<?> type, T value) {
        return contents.put(type.getName(), value);
    }

    /**
     * Associates the given value with the given fully qualified class name.
     */
    public T put(String className, T value) {
        return contents.put(className, value);
    }

    /**
     * Removes the mapping for the given type and returns the previous value, or {@code null}.
     */
    public T remove(Class<?> type) {
        return contents.remove(type.getName());
    }

    /**
     * Adds all entries from another TypeMap into this one.
     */
    public void putAll(TypeMap<? extends T> typeMap) {
        this.contents.putAll(typeMap.contents);
    }

    /**
     * Adds all entries from a {@code Map<Class<?>, T>} into this TypeMap.
     */
    public void putAll(Map<? extends Class<?>, ? extends T> map) {
        map.forEach(this::put);
    }

    /**
     * Executes the given consumer if a value for {@code type} is present.
     */
    public void ifPresent(Class<?> type, Consumer<T> consumer) {
        Optional.ofNullable(get(type)).ifPresent(consumer);
    }

    /**
     * Executes the given consumer if a value for {@code type} is present.
     * Note: Despite its name, this behaves like {@link #ifPresent(Class, Consumer)}.
     * Prefer using {@link #ifPresent(Class, Consumer)} directly.
     * @deprecated Misleading name; behaves like ifPresent.
     */
    @Deprecated
    public void ifAbsent(Class<?> type, Consumer<T> consumer) {
        Optional.ofNullable(get(type)).ifPresent(consumer);
    }

    /**
     * Computes a value using the supplier and stores it if absent; returns the existing or created value.
     */
    public T computeIfAbsent(Class<?> type, Supplier<T> supplier) {
        return contents.computeIfAbsent(type.getName(), (t) -> supplier.get());
    }

    /**
     * Replaces the existing value for {@code type} by applying the given function, if present.
     * @return the new value or {@code null} if none was present
     */
    public T computeIfPresent(Class<?> type, Function<T, T> function) {
        return contents.computeIfPresent(type.getName(), (key, existing) -> function.apply(existing));
    }

    /** Removes all entries from this map. */
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
