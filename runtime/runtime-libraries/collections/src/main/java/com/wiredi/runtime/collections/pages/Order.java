package com.wiredi.runtime.collections.pages;

import org.jetbrains.annotations.NotNull;

import static com.wiredi.runtime.collections.pages.Sort.DEFAULT_DIRECTION;

public record Order(
        @NotNull Sort.Direction direction,
        @NotNull String property,
        boolean ignoreCase,
        @NotNull Sort.NullHandling nullHandling
) {

    public static final boolean DEFAULT_IGNORE_CASE = false;
    public static final Sort.NullHandling DEFAULT_NULL_HANDLING = Sort.NullHandling.NATIVE;

    public static Builder builder(@NotNull String property) {
        return new Builder(property);
    }

    public static Order by(@NotNull String property) {
        return builder(property).build();
    }

    public static Order asc(@NotNull String property) {
        return builder(property).ascending().build();
    }

    public static Order desc(@NotNull String property) {
        return builder(property).descending().build();
    }

    public static class Builder {
        private @NotNull
        final String property;
        private @NotNull Sort.Direction direction = DEFAULT_DIRECTION;
        private boolean ignoreCase = DEFAULT_IGNORE_CASE;
        private @NotNull Sort.NullHandling nullHandling = DEFAULT_NULL_HANDLING;

        public Builder(@NotNull String property) {
            this.property = property;
        }

        public Builder withDirection(@NotNull Sort.Direction direction) {
            this.direction = direction;
            return this;
        }

        public Builder withIgnoreCase(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            return this;
        }

        public Builder withNullHandling(@NotNull Sort.NullHandling nullHandling) {
            this.nullHandling = nullHandling;
            return this;
        }

        public Builder caseSensitive() {
            return withIgnoreCase(false);
        }

        public Builder caseInsensitive() {
            return withIgnoreCase(true);
        }

        public Builder ascending() {
            return withDirection(Sort.Direction.ASC);
        }

        public Builder descending() {
            return withDirection(Sort.Direction.DESC);
        }

        public Builder nullsFirst() {
            return withNullHandling(Sort.NullHandling.NULLS_FIRST);
        }

        public Builder nullsLast() {
            return withNullHandling(Sort.NullHandling.NULLS_LAST);
        }

        public Builder nullsNative() {
            return withNullHandling(Sort.NullHandling.NATIVE);
        }

        public Order build() {
            return new Order(direction, property, ignoreCase, nullHandling);
        }
    }
}