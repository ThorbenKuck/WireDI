package com.wiredi.runtime.collections.pages;

public abstract class AbstractPage<T> implements Page<T> {

    private final int totalPages;
    private final long totalElements;
    private final Pageable pageable;

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
