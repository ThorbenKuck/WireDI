package com.wiredi.runtime.collections.pages;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class ListPage<T> extends AbstractPage<T> {

    private final List<T> content = new ArrayList<>();

    public ListPage(int totalPages, long totalElements, List<T> content, Pageable pageable) {
        super(totalPages, totalElements, pageable);
        this.content.addAll(content);
    }

    @Override
    public <U> Page<U> map(Function<T, U> converter) {
        return new ListPage<>(
                getTotalPages(),
                getTotalElements(),
                content.stream().map(converter).toList(),
                getCurrentPage()
        );
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return content.listIterator();
    }
}
