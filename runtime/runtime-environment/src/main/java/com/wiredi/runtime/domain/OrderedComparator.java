package com.wiredi.runtime.domain;

import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class OrderedComparator implements Comparator<Ordered> {

    /**
     * Shared default instance of {@code OrderComparator}.
     * <p>
     * As this class is stateless, it can be used as a classical singleton pattern.
     * <p>
     * Still, this is not recommended to be used in normal business code.
     * Instead, favor the methods of the {@link Ordered} interface, like {@link Ordered#ordered(List)},
     * or methods in here, like {@link #sort(Ordered[])}.
     */
    public static final OrderedComparator INSTANCE = new OrderedComparator();

    public static <T extends Ordered> List<T> sorted(List<T> list) {
        if (list.size() <= 1) {
            return list;
        }

        return list.stream()
                .sorted(INSTANCE)
                .toList();
    }

    public static <T extends Ordered> void sort(List<T> list) {
        if (list.size() > 1) {
            list.sort(INSTANCE);
        }
    }

    public static <T extends Ordered> void sort(T[] array) {
        if (array.length > 1) {
            Arrays.sort(array, INSTANCE);
        }
    }

    @Override
    public int compare(@Nullable Ordered o1, @Nullable Ordered o2) {
        int i1 = Optional.ofNullable(o1).map(Ordered::getOrder).orElse(Ordered.FIRST);
        int i2 = Optional.ofNullable(o2).map(Ordered::getOrder).orElse(Ordered.FIRST);
        return Ordered.compare(i1, i2);
    }
}
