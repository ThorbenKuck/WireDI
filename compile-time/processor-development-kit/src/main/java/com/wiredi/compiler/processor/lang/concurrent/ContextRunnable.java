package com.wiredi.compiler.processor.lang.concurrent;

import java.lang.reflect.UndeclaredThrowableException;

public interface ContextRunnable extends Runnable {

	default void beforeEach() { }

	default void afterEach() { }

	default void onError(Throwable throwable) {
		throw new UndeclaredThrowableException(throwable);
	}
}
