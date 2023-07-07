package com.wiredi.lang;

import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowingSupplier<T> {

	T get() throws Throwable;

	static <T> ThrowingSupplier<T> wrap(Supplier<T> supplier) {
		return supplier::get;
	}

	static <T> ThrowingSupplier<T> wrap(ThrowingRunnable runnable) {
		return () -> {
			runnable.run();
			return null;
		};
	}
}
