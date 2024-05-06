package com.wiredi.compiler.constructors;

import java.util.function.Supplier;

public interface TypeConstructor<T, S extends T> {

	S construct(Class<?> caller, Class<T> type);

	static <T, S extends T> TypeConstructor<T, S> wrap(Supplier<S> supplier) {
		return (caller, type) -> supplier.get();
	}

	default TypeConstructor<T, S> asSingleton() {
		return new SingletonTypeConstructor<>(this);
	}
}
