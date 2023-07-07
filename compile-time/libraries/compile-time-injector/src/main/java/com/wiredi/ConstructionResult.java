package com.wiredi;

public class ConstructionResult<T> {

	private final T value;
	private final boolean cache;

	private ConstructionResult(T value, boolean cache) {
		this.value = value;
		this.cache = cache;
	}

	public static <T> ConstructionResult<T> cached(T value) {
		return new ConstructionResult<>(value, true);
	}

	public static <T> ConstructionResult<T> doNotCache(T value) {
		return new ConstructionResult<>(value, false);
	}

	public T value() {
		return value;
	}

	public boolean cache() {
		return cache;
	}
}
