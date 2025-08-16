package com.wiredi.runtime.collections.pages;

import java.util.Collections;
import java.util.function.Function;

/**
 * Represents a single page (slice) of a larger result set along with pagination metadata.
 * A page is {@link Iterable} so you can iterate over its elements directly, and it exposes
 * the total number of elements and pages if known by the creator.
 * <p>
 * Pages are typically created by repositories or services. For simple in-memory use cases,
 * {@link ListPage} can be instantiated directly.
 * <p>
 * Example:
 * <pre>{@code
 * // Create a simple one-page result
 * Page<String> page = new ListPage<>(1, 3, List.of("a", "b", "c"), Pageable.ofSize(3));
 * for (String s : page) {
 *   System.out.println(s);
 * }
 *
 * // Transform a page to a different element type
 * Page<Integer> lengths = page.map(String::length);
 * }</pre>
 *
 * @param <T> the element type contained in the page
 */
public interface Page<T> extends Iterable<T> {

    /**
     * Creates an empty page using {@link Pageable#unpaged()}.
     */
    static <T> Page<T> empty() {
        return empty(Pageable.unpaged());
    }

    /**
     * Creates an empty page with the provided {@link Pageable} metadata.
     */
    static <T> Page<T> empty(Pageable pageable) {
        return new ListPage<>(0, 0, Collections.emptyList(), pageable);
    }

    /** Total number of pages for the full result set. */
    int getTotalPages();

    /** The {@link Pageable} describing this page request (paged or unpaged). */
    Pageable getCurrentPage();

    /** Total number of elements across all pages. */
    long getTotalElements();

    /**
     * Returns a new {@code Page<U>} with each element converted using the provided function.
     */
    <U> Page<U> map(Function<T, U> converter);

}
