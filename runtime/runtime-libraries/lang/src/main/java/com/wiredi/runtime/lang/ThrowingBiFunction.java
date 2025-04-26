package com.wiredi.runtime.lang;

@FunctionalInterface
public interface ThrowingBiFunction<T, U, R, E extends Throwable> {

	R apply(T t, U s) throws E;

}
