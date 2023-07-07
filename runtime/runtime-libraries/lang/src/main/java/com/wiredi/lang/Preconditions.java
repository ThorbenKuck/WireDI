package com.wiredi.lang;

import java.util.function.Supplier;

public class Preconditions {

	public static <T> T notNull(T instance, Supplier<String> message) {
		if (instance == null) {
			throw new IllegalArgumentException(message.get());
		}
		return instance;
	}

	public static <T> T notNull(T instance) {
		if (instance == null) {
			throw new NullPointerException();
		}
		return instance;
	}

	public static void require(boolean bool, Supplier<String> message) {
		if (!bool) {
			throw new IllegalArgumentException(message.get());
		}
	}
	public static void requireNot(boolean bool, Supplier<String> message) {
		if (bool) {
			throw new IllegalArgumentException(message.get());
		}
	}
}
