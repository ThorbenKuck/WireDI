package com.wiredi.domain;

import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class OrderComparator implements Comparator<Ordered> {

	/**
	 * Shared default instance of {@code OrderComparator}.
	 */
	public static final OrderComparator INSTANCE = new OrderComparator();

	public static void sort(List<? extends Ordered> list) {
		if (list.size() > 1) {
			list.sort(INSTANCE);
		}
	}

	public static void sort(Ordered[] array) {
		if (array.length > 1) {
			Arrays.sort(array, INSTANCE);
		}
	}

	@Override
	public int compare(@Nullable Ordered o1, @Nullable Ordered o2) {
		int i1 = Optional.ofNullable(o1).map(Ordered::getOrder).orElse(Ordered.FIRST);
		int i2 = Optional.ofNullable(o2).map(Ordered::getOrder).orElse(Ordered.FIRST);
		return Integer.compare(i1, i2);
	}
}
