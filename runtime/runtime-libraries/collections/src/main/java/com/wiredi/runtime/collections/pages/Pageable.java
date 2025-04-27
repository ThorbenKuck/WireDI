package com.wiredi.runtime.collections.pages;

import org.jetbrains.annotations.NotNull;

public interface Pageable {

	static Pageable unpaged() {
		return Unpaged.unsorted();
	}

	static Pageable unpaged(Sort sort) {
		return new Unpaged(sort);
	}

	static Pageable ofSize(int pageSize) {
		return new Paged(0, pageSize, Sort.unsorted());
	}

	default boolean isPaged() {
		return true;
	}

	default boolean isUnpaged() {
		return !isPaged();
	}

	int pageNumber();

	int pageSize();

	Sort sort();

	default Sort getSortOr(@NotNull Sort sort) {
		return sort().isSorted() ? sort() : sort;
	}
}
