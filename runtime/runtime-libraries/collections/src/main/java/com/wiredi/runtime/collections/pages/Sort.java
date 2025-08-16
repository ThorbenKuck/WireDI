package com.wiredi.runtime.collections.pages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A value object describing how to sort a result set. A Sort is a sequence of {@link Order} entries.
 * Use the static {@code by(...)} factory methods to construct Sort instances from properties or Order objects.
 * <p>
 * Example:
 * <pre>{@code
 * Sort s1 = Sort.by("lastName", "firstName");
 * Sort s2 = Sort.by(Sort.Direction.DESC, "createdAt");
 * Sort s3 = s1.and(s2); // combine multiple sort specifications
 * }</pre>
 */
public class Sort implements Iterable<Order> {

    /** The default direction when only properties are provided. */
    public static final Direction DEFAULT_DIRECTION = Direction.ASC;
    private static final Sort UNSORTED = Sort.by(new Order[0]);
    private final List<Order> orders;

    /**
     * Creates a Sort from a list of orders.
     */
    protected Sort(List<Order> orders) {
        this.orders = orders;
    }

    /**
     * Creates a Sort that sorts the given properties with the same direction.
     * @throws IllegalArgumentException if no properties are provided
     */
    private Sort(Direction direction, @NotNull List<String> properties) {
        if (properties.isEmpty()) {
            throw new IllegalArgumentException("You have to provide at least one property to sort by");
        }

        this.orders = properties.stream()
                .map(it -> Order.builder(it).withDirection(direction).build())
                .collect(Collectors.toList());
    }

    /**
     * Creates a Sort by the given properties using {@link #DEFAULT_DIRECTION}.
     * Returns {@link #unsorted()} if no properties are provided.
     */
    public static Sort by(@NotNull String... properties) {
        return properties.length == 0
                ? Sort.unsorted()
                : new Sort(DEFAULT_DIRECTION, Arrays.asList(properties));
    }

    /** Creates a Sort from the provided order list, or {@link #unsorted()} if empty. */
    public static Sort by(@NotNull List<Order> orders) {
        return orders.isEmpty()
                ? Sort.unsorted()
                : new Sort(orders);
    }

    /** Creates a Sort from the provided order array. */
    public static Sort by(@NotNull Order... orders) {
        return orders.length == 0
                ? Sort.unsorted()
                : new Sort(Arrays.asList(orders));
    }

    /**
     * Creates a Sort with the same direction for all given properties.
     * @throws IllegalArgumentException if no properties are provided
     */
    public static Sort by(@NotNull Direction direction, @NotNull String... properties) {
        if (properties.length == 0) {
            throw new IllegalArgumentException("You have to provide at least one property to sort by");
        }

        return Sort.by(Arrays.stream(properties)
                .map(it -> Order.builder(it).withDirection(direction).build())
                .collect(Collectors.toList()));
    }

    /** Returns a shared unsorted instance that carries no orders. */
    public static Sort unsorted() {
        return UNSORTED;
    }

    /** True if at least one {@link Order} is present. */
    public boolean isSorted() {
        return !isEmpty();
    }

    /** True if no orders are present. */
    public boolean isEmpty() {
        return orders.isEmpty();
    }

    /** True if no orders are present (alias for !isSorted()). */
    public boolean isUnsorted() {
        return !isSorted();
    }

    /**
     * Concatenates this sort with another one, returning a new Sort with combined orders.
     */
    public Sort and(@NotNull Sort sort) {
        List<Order> these = new ArrayList<>(orders);

        for (Order order : sort) {
            these.add(order);
        }

        return Sort.by(these);
    }

    /**
     * Returns the order for the given property or {@code null} if not present.
     */
    @Nullable
    public Order getOrderFor(String property) {

        for (Order order : this) {
            if (order.property().equals(property)) {
                return order;
            }
        }

        return null;
    }

    @Override
    public Iterator<Order> iterator() {
        return this.orders.iterator();
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Sort that)) {
            return false;
        }

        return orders.equals(that.orders);
    }

    @Override
    public int hashCode() {

        int result = 17;
        result = 31 * result + orders.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return isEmpty() ? "UNSORTED" : "Sort" + orders;
    }

    /**
     * Direction of sorting for a single property.
     */
    public enum Direction {

        /** Ascending order (smallest to largest). */
        ASC,
        /** Descending order (largest to smallest). */
        DESC;

        /** Default direction when none is specified. */
        public static final Direction DEFAULT = ASC;

        /**
         * Parses the given string into a Direction, case-insensitive.
         * Accepts "asc" or "desc".
         * @throws IllegalArgumentException if the input cannot be parsed
         */
        public static Direction parse(String value) {

            try {
                return Direction.valueOf(value.toUpperCase(Locale.US));
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(
                        "Invalid value '%s' for orders given; Has to be either 'desc' or 'asc' (case insensitive)", value), e);
            }
        }

        /** True if this is {@link #ASC}. */
        public boolean isAscending() {
            return this.equals(ASC);
        }

        /** True if this is {@link #DESC}. */
        public boolean isDescending() {
            return this.equals(DESC);
        }
    }

    /**
     * Defines how null values should be treated during comparison.
     */
    public enum NullHandling {
        /** Use the database or backend native null ordering. */
        NATIVE,
        /** Place nulls before non-nulls. */
        NULLS_FIRST,
        /** Place nulls after non-nulls. */
        NULLS_LAST;
    }
}
