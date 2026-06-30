package com.wiredi.runtime.domain.provider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TypeIdentifierMatcher {

    // Optimization 1: Cache distances globally. Class hierarchies are immutable at runtime.
    // Using a Java record as a lightweight compound key.
    private record ClassPair(Class<?> child, Class<?> parent) {}
    private static final Map<ClassPair, Integer> DISTANCE_CACHE = new ConcurrentHashMap<>(256);

    public static <T> List<TypeIdentifier<? extends T>> filterAndSort(
            Collection<TypeIdentifier<?>> candidates,
            TypeIdentifier<T> target
    ) {
        return filterAndSort(candidates, target, genericFocusStrategy(target));
    }

    /**
     * Filters and sorts the candidates. Optimized to avoid stream overhead and
     * minimize array resizing allocations during the DI context startup.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<TypeIdentifier<? extends T>> filterAndSort(
            Collection<TypeIdentifier<?>> candidates,
            TypeIdentifier<T> target,
            Comparator<TypeIdentifier<?>> strategy
    ) {
        // Pre-size the array list to avoid internal array copying/growth
        List<TypeIdentifier<? extends T>> result = new ArrayList<>(candidates.size());

        for (TypeIdentifier<?> candidate : candidates) {
            if (candidate.isInstanceOf(target)) {
                result.add((TypeIdentifier<? extends T>) candidate);
            }
        }

        result.sort(strategy);
        return result;
    }

    public static Comparator<TypeIdentifier<?>> rootFocusStrategy(TypeIdentifier<?> target) {
        return (a, b) -> {
            int rootDistA = getRootDistance(a, target);
            int rootDistB = getRootDistance(b, target);

            if (rootDistA != rootDistB) {
                return Integer.compare(rootDistA, rootDistB);
            }

            return Integer.compare(getGenericDistance(a, target), getGenericDistance(b, target));
        };
    }

    public static Comparator<TypeIdentifier<?>> genericFocusStrategy(TypeIdentifier<?> target) {
        return (a, b) -> {
            int genDistA = getGenericDistance(a, target);
            int genDistB = getGenericDistance(b, target);

            if (genDistA != genDistB) {
                return Integer.compare(genDistA, genDistB);
            }

            return Integer.compare(getRootDistance(a, target), getRootDistance(b, target));
        };
    }

    private static int getRootDistance(TypeIdentifier<?> candidate, TypeIdentifier<?> target) {
        return getClassDistance(candidate.getRootType(), target.getRootType());
    }

    private static int getGenericDistance(TypeIdentifier<?> candidate, TypeIdentifier<?> target) {
        int totalDistance = 0;
        List<TypeIdentifier<?>> candGenerics = candidate.getGenericTypes();
        List<TypeIdentifier<?>> targetGenerics = target.getGenericTypes();

        // Standard loops are slightly faster than streams here and prevent allocation
        int size = Math.min(candGenerics.size(), targetGenerics.size());
        for (int i = 0; i < size; i++) {
            totalDistance += getTypeIdentifierDistance(candGenerics.get(i), targetGenerics.get(i));
        }
        return totalDistance;
    }

    private static int getTypeIdentifierDistance(TypeIdentifier<?> candidate, TypeIdentifier<?> target) {
        return getRootDistance(candidate, target) + getGenericDistance(candidate, target);
    }

    private static int getClassDistance(Class<?> child, Class<?> parent) {
        if (child == parent) {
            return 0;
        }
        // Thread-safe O(1) lookup after the first resolution
        return DISTANCE_CACHE.computeIfAbsent(new ClassPair(child, parent), key -> computeClassDistance(key.child, key.parent));
    }

    private static int computeClassDistance(Class<?> child, Class<?> parent) {
        Queue<Class<?>> queue = new LinkedList<>();
        Map<Class<?>, Integer> distances = new HashMap<>();

        queue.add(child);
        distances.put(child, 0);

        while (!queue.isEmpty()) {
            Class<?> current = queue.poll();
            int currentDist = distances.get(current);

            if (current == parent) {
                return currentDist;
            }

            // Optimization 2: Avoid structural allocations like List.add() or Collections.addAll()
            // inside the core BFS loop. Directly query the class definitions.
            Class<?> superclass = current.getSuperclass();
            if (superclass != null && !distances.containsKey(superclass)) {
                distances.put(superclass, currentDist + 1);
                queue.add(superclass);
            }

            for (Class<?> iface : current.getInterfaces()) {
                if (!distances.containsKey(iface)) {
                    distances.put(iface, currentDist + 1);
                    queue.add(iface);
                }
            }
        }

        return Integer.MAX_VALUE;
    }
}