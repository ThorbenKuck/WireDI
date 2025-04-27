package com.wiredi.runtime.collections.pages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class Sort implements Iterable<Order> {

    public static final Direction DEFAULT_DIRECTION = Direction.ASC;
    private static final Sort UNSORTED = Sort.by(new Order[0]);
    private final List<Order> orders;

    protected Sort(List<Order> orders) {
        this.orders = orders;
    }

    private Sort(Direction direction, @NotNull List<String> properties) {
        if (properties.isEmpty()) {
            throw new IllegalArgumentException("You have to provide at least one property to sort by");
        }

        this.orders = properties.stream()
                .map(it -> Order.builder(it).withDirection(direction).build())
                .collect(Collectors.toList());
    }

    public static Sort by(@NotNull String... properties) {
        return properties.length == 0 //
                ? Sort.unsorted() //
                : new Sort(DEFAULT_DIRECTION, Arrays.asList(properties));
    }

    public static Sort by(@NotNull List<Order> orders) {
        return orders.isEmpty() ? Sort.unsorted() : new Sort(orders);
    }

    public static Sort by(@NotNull Order... orders) {
        return new Sort(Arrays.asList(orders));
    }

    public static Sort by(@NotNull Direction direction, @NotNull String... properties) {
        if (properties.length == 0) {
            throw new IllegalArgumentException("You have to provide at least one property to sort by");
        }

        return Sort.by(Arrays.stream(properties)
                .map(it -> Order.builder(it).withDirection(direction).build())
                .collect(Collectors.toList()));
    }

    public static Sort unsorted() {
        return UNSORTED;
    }

    public boolean isSorted() {
        return !isEmpty();
    }

    public boolean isEmpty() {
        return orders.isEmpty();
    }

    public boolean isUnsorted() {
        return !isSorted();
    }

    public Sort and(@NotNull Sort sort) {
        List<Order> these = new ArrayList<>(orders);

        for (Order order : sort) {
            these.add(order);
        }

        return Sort.by(these);
    }

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

    public enum Direction {

        ASC, DESC;

        public static final Direction DEFAULT = ASC;

        public static Direction parse(String value) {

            try {
                return Direction.valueOf(value.toUpperCase(Locale.US));
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(
                        "Invalid value '%s' for orders given; Has to be either 'desc' or 'asc' (case insensitive)", value), e);
            }
        }

        public boolean isAscending() {
            return this.equals(ASC);
        }

        public boolean isDescending() {
            return this.equals(DESC);
        }
    }

    public enum NullHandling {
        NATIVE,
        NULLS_FIRST,
        NULLS_LAST;
    }
}
