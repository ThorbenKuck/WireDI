package com.wiredi.runtime.lang;

import java.util.function.Supplier;

public record SingletonSupplier<T>(T value) implements Supplier<T> {
	@Override
	public T get() {
		return value;
	}
}
