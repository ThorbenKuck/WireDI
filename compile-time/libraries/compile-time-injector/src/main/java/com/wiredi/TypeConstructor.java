package com.wiredi;

import java.util.function.Supplier;

public interface TypeConstructor<T, S extends T> {

	ConstructionResult<S> construct(Class<?> caller, Class<T> type);

	static <T, S extends T> TypeConstructor<T, S> wrap(Supplier<S> supplier) {
		return (caller, type) -> ConstructionResult.cached(supplier.get());
	}
}
