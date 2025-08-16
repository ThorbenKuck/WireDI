package com.wiredi.runtime.collections.pages;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * A concrete {@link Page} implementation that stores its elements in a {@link List}.
 * This is a simple, in-memory page used for returning already-materialized results.
 * <p>
 * Example:
 * <pre>{@code
 * List<String> data = List.of("a", "b", "c");
 * Pageable pageable = new Paged(0, 3, Sort.unsorted());
 * Page<String> page = new ListPage<>(1, 3, data, pageable);
 *
 * // Transform the page content
 * Page<Integer> lengths = page.map(String::length);
 * }</pre>
 */
public class ListPage<T> extends AbstractPage<T> {

    private final List<T> content = new ArrayList<>();

    /**
     * Creates a page backed by the provided content list.
     * @param totalPages the total number of pages for the entire result set
     * @param totalElements the total number of elements across all pages
     * @param content the content of this page
     * @param pageable the descriptor of the current page request
     */
    public ListPage(int totalPages, long totalElements, List<T> content, Pageable pageable) {
        super(totalPages, totalElements, pageable);
        this.content.addAll(content);
    }

    /**
     * Maps the content of this page to a page of a different type using the provided converter.
     */
    @Override
    public <U> Page<U> map(Function<T, U> converter) {
        return new ListPage<>(
                getTotalPages(),
                getTotalElements(),
                content.stream().map(converter).toList(),
                getCurrentPage()
        );
    }

    /** Returns an iterator over the page content. */
    @Override
    public @NotNull Iterator<T> iterator() {
        return content.listIterator();
    }
}
