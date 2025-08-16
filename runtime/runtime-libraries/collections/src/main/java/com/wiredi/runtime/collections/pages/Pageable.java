package com.wiredi.runtime.collections.pages;

import org.jetbrains.annotations.NotNull;

/**
 * Describes a request for a particular page in a larger result set, including the desired page number,
 * page size, and sort. Implementations include {@link Paged} for actual pagination and {@link Unpaged}
 * for indicating that no pagination is requested.
 * <p>
 * Example:
 * <pre>{@code
 * Pageable p = new Paged(2, 25, Sort.by("createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
 * if (p.isPaged()) {
 *   int offset = p.pageNumber() * p.pageSize();
 * }
 * }</pre>
 */
public interface Pageable {

    /** Creates an unpaged descriptor without sorting. */
    static Pageable unpaged() {
        return Unpaged.unsorted();
    }

    /** Creates an unpaged descriptor with the given sort. */
    static Pageable unpaged(Sort sort) {
        return new Unpaged(sort);
    }

    /** Creates a paged descriptor for the first page with the given page size. */
    static Pageable ofSize(int pageSize) {
        return new Paged(0, pageSize, Sort.unsorted());
    }

    /** True if this descriptor represents a paged request. */
    default boolean isPaged() {
        return true;
    }

    /** True if this descriptor represents an unpaged request. */
    default boolean isUnpaged() {
        return !isPaged();
    }

    /** The zero-based page number to retrieve. */
    int pageNumber();

    /** The maximum number of elements per page. */
    int pageSize();

    /** The {@link Sort} to apply to the result set. */
    Sort sort();

    /** Returns {@link #sort()} if sorted, otherwise returns the provided default. */
    default Sort getSortOr(@NotNull Sort sort) {
        return sort().isSorted() ? sort() : sort;
    }
}
