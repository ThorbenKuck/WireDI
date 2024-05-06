package com.wiredi.runtime.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class Preconditions {

	@NotNull
	public static <T> T isNotNull(@Nullable T instance, @NotNull Supplier<String> message) {
		if (instance == null) {
			throw new NullPointerException(message.get());
		}
		return instance;
	}

	@NotNull
	public static <T> T isNotNull(@Nullable T instance) {
		if (instance == null) {
			throw new NullPointerException();
		}
		return instance;
	}

	public static <T> void isNull(@Nullable T instance, @NotNull Supplier<String> message) {
		if (instance != null) {
			throw new IllegalStateException(message.get());
		}
	}

	public static <T> void isNull(@Nullable T instance) {
		if (instance == null) {
			throw new IllegalStateException();
		}
	}

	public static void is(boolean bool, @NotNull Supplier<String> message) {
		if (!bool) {
			throw new IllegalStateException(message.get());
		}
	}
	public static void isNot(boolean bool, @NotNull Supplier<String> message) {
		if (bool) {
			throw new IllegalStateException(message.get());
		}
	}
}
