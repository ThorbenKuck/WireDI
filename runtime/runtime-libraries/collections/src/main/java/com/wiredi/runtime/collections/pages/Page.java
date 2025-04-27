package com.wiredi.runtime.collections.pages;

import java.util.Collections;
import java.util.function.Function;

public interface Page<T> extends Iterable<T> {

    static <T> Page<T> empty() {
        return empty(Pageable.unpaged());
    }

    static <T> Page<T> empty(Pageable pageable) {
        return new ListPage<>(0, 0, Collections.emptyList(), pageable);
    }

    int getTotalPages();

    Pageable getCurrentPage();

    long getTotalElements();

    <U> Page<U> map(Function<T, U> converter);

}
