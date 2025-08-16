package com.wiredi.runtime.collections.pages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable {@link Pageable} implementation representing a concrete paged request.
 * It carries a zero-based page number, a page size and a {@link Sort} descriptor.
 * <p>
 * Example:
 * <pre>{@code
 * Pageable p = new Paged(1, 20, Sort.by("lastName", "firstName"));
 * int offset = p.pageNumber() * p.pageSize(); // 20
 * }</pre>
 *
 * @param pageNumber the zero-based page index to fetch
 * @param pageSize the maximum number of items per page
 * @param sort the sort to apply (never null; falls back to {@link Sort#unsorted()} if constructed with null)
 */
public record Paged(int pageNumber, int pageSize, @NotNull Sort sort) implements Pageable {

    /**
     * Creates a Paged descriptor and replaces a null sort with {@link Sort#unsorted()}.
     */
    public Paged(int pageNumber, int pageSize, @Nullable Sort sort) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sort = sort == null ? Sort.unsorted() : sort;
    }
}
