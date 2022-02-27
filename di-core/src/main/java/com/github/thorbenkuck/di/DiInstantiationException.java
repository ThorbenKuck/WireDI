package com.github.thorbenkuck.di;

import org.jetbrains.annotations.NotNull;

public class DiInstantiationException extends RuntimeException {

	public DiInstantiationException(@NotNull final String message) {
		super(message);
	}

	public DiInstantiationException(
			@NotNull final String message,
			@NotNull final Throwable cause
	) {
		super(message, cause);
	}
}
