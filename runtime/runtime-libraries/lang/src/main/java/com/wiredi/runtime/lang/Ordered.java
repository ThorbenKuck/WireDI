package com.wiredi.runtime.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This interface marks the implementing class as a class that can be ordered in accordance to the WireDI order logic.
 * <p>
 * The ordering of classes implementing this interface is ascending.
 * Lower values will be in earlier positions of the ordered collection.
 * When iterating an ordered list of {@link Ordered} implementations, the first element will be the one with the
 * lowest order, whilst the last element will be the one with the highest order.
 * <p>
 * The following list {@code [10, 7, 1, -9, 3]} would be ordered to {@code [-9, 1, 3, 7, 10]}.
 * <p>
 * This interface is allowing for integration into the existing {@link Comparable} logic of java.
 * If multiple Ordered instances have the same order, the original order is preserved.
 * <p>
 * If any element in the collection does not implement this interface, it is considered to be a {@link Ordered#FIRST}.
 * <p>
 * <b>Relative orders</b>
 * <p>
 * You can use orders "absolute", i.e., you can return any number in the {@link Ordered#getOrder()} method.
 * However, sometimes you want to make sure that a certain element is ordered before or after another one.
 * For that you can use the {@link Ordered#before(Integer)} and {@link Ordered#after(Integer)} methods.
 * <p>
 *
 * @see OrderedComparator
 */
public interface Ordered extends Comparable<Ordered> {

    int LAST = Integer.MAX_VALUE;
    int FIRST = Integer.MIN_VALUE;
    int DEFAULT = 0;

    static int compare(Ordered o1, Ordered o2) {
        return Integer.compare(o1.getOrder(), o2.getOrder());
    }

    static int compare(int i1, int i2) {
        return Integer.compare(i1, i2);
    }

    /**
     * Returns a new, ordered list with the same elements as {@code input}
     *
     * @param input the list to sort.
     * @param <T>   the type of order.
     * @return a new, ordered list.
     * @see OrderedComparator#sorted(List)
     */
    static <T extends Ordered> List<T> ordered(List<T> input) {
        return OrderedComparator.sorted(input);
    }

    /**
     * Orders the provided {@code input} list according to the contract.
     * <p>
     * The list will stay the same and be updated.
     *
     * @param input the list to order
     * @see OrderedComparator#sort(List)
     */
    static void order(List<? extends Ordered> input) {
        OrderedComparator.sort(input);
    }

    /**
     * Calculates an order after the provided one.
     * <p>
     * This can be used to show relations between Ordered instances.
     *
     * @param integer the order that will be before this
     * @return an order number that is after the {@code integer}
     */
    static int after(@Nullable Integer integer) {
        if (integer == null || integer == LAST) {
            return LAST;
        }

        return integer + 1;
    }

    /**
     * Calculates an order before the provided one.
     * <p>
     * This can be used to show relations between Ordered instances.
     *
     * @param integer the order that will be after this
     * @return an order number that is before the {@code integer}
     */
    static int before(@Nullable Integer integer) {
        if (integer == null || integer == FIRST) {
            return FIRST;
        }

        return integer - 1;
    }

    /**
     * {@inheritDoc}
     *
     * @see #compare(int, int)
     * @see #compare(Ordered, Ordered)
     */
    @Override
    default int compareTo(@NotNull Ordered o) {
        return compare(this, o);
    }

    /**
     * Returns the order of this instance.
     *
     * @return the concrete order
     */
    default int getOrder() {
        return DEFAULT;
    }
}
