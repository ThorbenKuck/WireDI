package com.github.thorbenkuck.di;

import org.jetbrains.annotations.NotNull;

public class DiLoadingException extends RuntimeException {

	public DiLoadingException(@NotNull final String message) {
		super(message);
	}

	public DiLoadingException(
			@NotNull final String message,
			@NotNull final Throwable cause
	) {
		super(message, cause);
	}
}
