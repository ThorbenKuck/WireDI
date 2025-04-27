package com.wiredi.runtime.collections.pages;

import org.jetbrains.annotations.NotNull;

public class Unpaged implements Pageable {

    private static final Pageable UNSORTED = new Unpaged(Sort.unsorted());

    @NotNull
    private final Sort sort;

    public static Pageable unsorted() {
        return UNSORTED;
    }

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

    @Override
    public int pageSize() {
        throw new UnsupportedOperationException();
    }

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
