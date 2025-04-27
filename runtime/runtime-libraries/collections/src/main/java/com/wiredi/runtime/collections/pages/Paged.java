package com.wiredi.runtime.collections.pages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Paged(int pageNumber, int pageSize, @NotNull Sort sort) implements Pageable {

    public Paged(int pageNumber, int pageSize, @Nullable Sort sort) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sort = sort == null ? Sort.unsorted() : sort;
    }
}
