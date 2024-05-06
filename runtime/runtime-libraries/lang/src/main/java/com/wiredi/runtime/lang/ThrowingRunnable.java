package com.wiredi.runtime.lang;

public interface ThrowingRunnable<E extends Throwable> {

	void run() throws E;

}
