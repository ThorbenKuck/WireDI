package com.wiredi.runtime.collections.pages;

/**
 * A small base class for {@link Page} implementations that holds common pagination metadata:
 * total pages, total elements and the {@link Pageable} describing the current slice.
 * Subclasses only need to provide content handling (iteration and mapping).
 */
public abstract class AbstractPage<T> implements Page<T> {

    private final int totalPages;
    private final long totalElements;
    private final Pageable pageable;

    /**
     * Creates a page with the given metadata.
     * @param totalPages the total number of pages for the entire result set
     * @param totalElements the total number of elements across all pages
     * @param pageable the descriptor of the current page request
     */
    protected AbstractPage(int totalPages, long totalElements, Pageable pageable) {
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.pageable = pageable;
    }

    @Override
    public final int getTotalPages() {
        return totalPages;
    }

    @Override
    public final long getTotalElements() {
        return totalElements;
    }

    @Override
    public Pageable getCurrentPage() {
        return pageable;
    }
}
