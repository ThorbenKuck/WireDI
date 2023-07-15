package com.wiredi.domain;

import com.wiredi.annotations.Order;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Ordered extends Comparable<Ordered> {

	int LAST = Order.LAST;
	int FIRST = Order.FIRST;
	int DEFAULT = Order.DEFAULT;

	@Override
	default int compareTo(@NotNull Ordered o) {
		return OrderComparator.INSTANCE.compare(this, o);
	}

	static int compare(Ordered o1, Ordered o2) {
		return OrderComparator.INSTANCE.compare(o1, o2);
	}

	static <T extends Ordered> List<T> ordered(List<T> input) {
		return input.stream().sorted(OrderComparator.INSTANCE).toList();
	}

	static void order(List<? extends Ordered> input) {
		input.sort(OrderComparator.INSTANCE);
	}

	static int after(Integer integer) {
		if (integer == null || integer == LAST) {
			return LAST;
		}

		return integer + 1;
	}

	static int before(Integer integer) {
		if (integer == null || integer == FIRST) {
			return FIRST;
		}

		return integer - 1;
	}

	default int getOrder() {
		return DEFAULT;
	}
}
