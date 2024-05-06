package com.wiredi.runtime.collections;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentTypeMap<T> extends TypeMap<T> {

    public ConcurrentTypeMap(int initialCapacity) {
        super(new ConcurrentHashMap<>(initialCapacity));
    }

    public ConcurrentTypeMap(int initialCapacity, float loadFactor) {
        super(new ConcurrentHashMap<>(initialCapacity, loadFactor));
    }

    public ConcurrentTypeMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        super(new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel));
    }

    public ConcurrentTypeMap(TypeMap<T> other) {
        super(new ConcurrentHashMap<>(other.contents));
    }

    public ConcurrentTypeMap() {
        super(new ConcurrentHashMap<>());
    }

    public ConcurrentTypeMap(Map<String, T> contents) {
        super(new ConcurrentHashMap<>(contents));
    }
}
