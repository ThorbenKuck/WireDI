package com.wiredi.runtime.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class InMemoryCache<K, V> implements Cache<K, V>{

    private final @NotNull Map<@Nullable K, @NotNull Node<K, V>> map;
    private final @NotNull CacheConfiguration configuration;
    @VisibleForTesting
    @Nullable
    Node<K, V> first;
    @VisibleForTesting
    @Nullable
    Node<K, V> last;
    private int size;

    public InMemoryCache(int capacity) {
        this(CacheConfiguration.newInstance().withCapacity(capacity).build());
    }

    public InMemoryCache() {
        this(CacheConfiguration.DEFAULT);
    }

    public InMemoryCache(@NotNull CacheConfiguration configuration) {
        this.configuration = configuration;
        this.map = new HashMap<>(configuration.capacity(), 1.1f);
    }

    @Override
    public Cache<K, V> put(@Nullable K key, @NotNull V value) {
        Node<K, V> existing = map.get(key);
        if(existing == null) {
            Node<K, V> node = new Node<>(key, value);
            if(size() >= configuration.capacity()) {
                invalidateNext();
            }
            addNodeToLast(node);
            map.put(key, node);
        } else {
            existing.value = value;
            if (configuration.hitOnOverride()) {
                hit(existing);
            }
        }

        return this;
    }

    @Override
    public @NotNull Optional<V> get(@Nullable K key) {
        return Optional.ofNullable(map.get(key)).map(node -> {
            hit(node);
            return node.value;
        });
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Cache<K, V> invalidate() {
        map.clear();
        @Nullable Node<K, V> pointer = first;
        first = null;
        last = null;
        while (pointer != null) {
            pointer = pointer.invalidateAndGetNext();
        }

        return this;
    }

    @Override
    public Cache<K, V> invalidate(@Nullable K k) {
        @Nullable Node<K, V> node = map.get(k);
        if (node != null) {
            invalidate(node);
        }
        return this;
    }

    private void hit(@NotNull Node<K, V> node) {
        node.incrementHitCount();
        if (configuration.reorderOnHit()) {
            reorder(node);
        }
    }

    private void invalidateNext() {
        if (first == null) {
            return;
        }
        if(first == last) {
            invalidate(first);
            return;
        }

        Node<K, V> invalidationTarget = first;
        while(invalidationTarget.next != null && invalidationTarget.next.hitCount < invalidationTarget.hitCount) {
            invalidationTarget = invalidationTarget.next;
        }
        invalidate(invalidationTarget);
    }

    private void reorder(@NotNull Node<K, V> node) {
        if(last == node) {
            return;
        }
        if(first == node) {
            first = node.next;
            first.prev = null;
        } else {
            node.prev.next = node.next;
            node.next.prev = node.prev;

        }
        last.next = node;
        node.prev = last;
        last = node;
        node.next = null;
    }

    private void reorderWithLFU(@NotNull Node<K, V> node) {
        if(last == node) {
            return;
        }
        @Nullable Node<K, V> nextNode = node.next;
        while (nextNode != null) {
            if(nextNode.hitCount > node.hitCount) {
                break;
            }
            if(first == node) {
                first = nextNode;
            }
            if(node.prev != null) {
                node.prev.next = nextNode;
            }
            nextNode.prev = node.prev;
            node.prev = nextNode;
            node.next = nextNode.next;
            if(nextNode.next != null) {
                nextNode.next.prev = node;
            }
            nextNode.next = node;
            nextNode = node.next;
        }
        if(node.next == null) {
            last = node;
        }
    }

    private void addNodeToLast(@NotNull Node<K, V> node) {
        if(last != null) {
            last.next = node;
            node.prev = last;
        }

        last = node;
        if(first == null) {
            first = node;
        }
        size++;
    }

    private void invalidate(@NotNull Node<K, V> node) {
        if(last == node) {
            last = node.prev;
            last.next = null;
        } else if(first == node) {
            first = node.next;
            first.prev = null;
        } else {
            node.next.prev = node.prev;
            node.prev.next = node.next;
        }
        map.remove(node.key);
        size--;
        node.prev = null;
        node.next = null;
    }

    @VisibleForTesting
    static final class Node<K, V> {
        @Nullable
        @VisibleForTesting
        final K key;

        @Nullable
        @VisibleForTesting
        V value;

        private long hitCount = 0;

        @Nullable
        @VisibleForTesting
        Node<K, V> prev, next;

        public Node(@Nullable K key, @Nullable V value) {
            this.value = value;
            this.key = key;
        }

        public @Nullable Node<K, V> invalidateAndGetNext() {
            Node<K, V> returnValue = next;
            prev = null;
            next = null;
            return returnValue;
        }

        public void incrementHitCount() {
            if (this.hitCount != Long.MAX_VALUE) {
                this.hitCount++;
            }
        }

        @Override
        public String toString() {
            return "CacheItem(" + key + "," + value + ")";
        }
    }
}
