package com.wiredi.runtime.collections.pages;

import org.jetbrains.annotations.NotNull;

import static com.wiredi.runtime.collections.pages.Sort.DEFAULT_DIRECTION;

/**
 * Describes a single property sorting instruction: which property to sort by, in which direction,
 * whether to ignore case, and how to handle nulls. Used inside {@link Sort} to describe a full ordering.
 * <p>
 * Example:
 * <pre>{@code
 * Order o1 = Order.asc("lastName");
 * Order o2 = Order.builder("createdAt")
 *     .descending()
 *     .nullsLast()
 *     .build();
 * Sort sort = Sort.by(o1, o2);
 * }</pre>
 */
public record Order(
        @NotNull Sort.Direction direction,
        @NotNull String property,
        boolean ignoreCase,
        @NotNull Sort.NullHandling nullHandling
) {

    /** Default for case sensitivity: case-sensitive comparisons. */
    public static final boolean DEFAULT_IGNORE_CASE = false;
    /** Default for null ordering: use backend/database native behavior. */
    public static final Sort.NullHandling DEFAULT_NULL_HANDLING = Sort.NullHandling.NATIVE;

    /** Starts building an Order for the given property. */
    public static Builder builder(@NotNull String property) {
        return new Builder(property);
    }

    /** Shortcut to build a default order for the given property. */
    public static Order by(@NotNull String property) {
        return builder(property).build();
    }

    /** Shortcut to build an ascending order for the given property. */
    public static Order asc(@NotNull String property) {
        return builder(property).ascending().build();
    }

    /** Shortcut to build a descending order for the given property. */
    public static Order desc(@NotNull String property) {
        return builder(property).descending().build();
    }

    /** Builder for {@link Order}. */
    public static class Builder {
        private @NotNull
        final String property;
        private @NotNull Sort.Direction direction = DEFAULT_DIRECTION;
        private boolean ignoreCase = DEFAULT_IGNORE_CASE;
        private @NotNull Sort.NullHandling nullHandling = DEFAULT_NULL_HANDLING;

        public Builder(@NotNull String property) {
            this.property = property;
        }

        /** Sets the direction (ascending/descending). */
        public Builder withDirection(@NotNull Sort.Direction direction) {
            this.direction = direction;
            return this;
        }

        /** Enables or disables case-insensitive comparison. */
        public Builder withIgnoreCase(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            return this;
        }

        /** Sets how nulls should be ordered. */
        public Builder withNullHandling(@NotNull Sort.NullHandling nullHandling) {
            this.nullHandling = nullHandling;
            return this;
        }

        /** Sets case-sensitive ordering (default). */
        public Builder caseSensitive() {
            return withIgnoreCase(false);
        }

        /** Sets case-insensitive ordering. */
        public Builder caseInsensitive() {
            return withIgnoreCase(true);
        }

        /** Sets ascending ordering. */
        public Builder ascending() {
            return withDirection(Sort.Direction.ASC);
        }

        /** Sets descending ordering. */
        public Builder descending() {
            return withDirection(Sort.Direction.DESC);
        }

        /** Places nulls before non-nulls. */
        public Builder nullsFirst() {
            return withNullHandling(Sort.NullHandling.NULLS_FIRST);
        }

        /** Places nulls after non-nulls. */
        public Builder nullsLast() {
            return withNullHandling(Sort.NullHandling.NULLS_LAST);
        }

        /** Uses backend/database native null ordering. */
        public Builder nullsNative() {
            return withNullHandling(Sort.NullHandling.NATIVE);
        }

        /** Builds the {@link Order}. */
        public Order build() {
            return new Order(direction, property, ignoreCase, nullHandling);
        }
    }
}