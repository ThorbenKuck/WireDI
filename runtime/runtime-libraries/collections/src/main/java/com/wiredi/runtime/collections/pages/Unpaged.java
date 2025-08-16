package com.wiredi.runtime.collections.pages;

import org.jetbrains.annotations.NotNull;

/**
 * A {@link Pageable} implementation indicating that no pagination should be applied.
 * It can still carry a {@link Sort} to describe the desired ordering while fetching all results.
 * <p>
 * Example:
 * <pre>{@code
 * Pageable unpaged = Pageable.unpaged(Sort.by("createdAt"));
 * if (unpaged.isUnpaged()) {
 *   // fetch all results using the given sort
 * }
 * }</pre>
 */
public class Unpaged implements Pageable {

    private static final Pageable UNSORTED = new Unpaged(Sort.unsorted());

    @NotNull
    private final Sort sort;

    /** Returns a shared unpaged instance with {@link Sort#unsorted()}. */
    public static Pageable unsorted() {
        return UNSORTED;
    }

    /** Creates an unpaged descriptor with the given sort. */
    Unpaged(@NotNull Sort sort) {
        this.sort = sort;
    }

    @Override
    public boolean isPaged() {
        return false;
    }

    @Override
    public Sort sort() {
        return sort;
    }

    /** This operation is unsupported for unpaged requests. */
    @Override
    public int pageSize() {
        throw new UnsupportedOperationException();
    }

    /** This operation is unsupported for unpaged requests. */
    @Override
    public int pageNumber() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Unpaged unpaged)) {
            return false;
        }

        return sort.equals(unpaged.sort);
    }

    @Override
    public int hashCode() {
        return sort.hashCode();
    }

}
