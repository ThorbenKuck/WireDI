package com.github.thorbenkuck.di.runtime.exceptions;

import org.jetbrains.annotations.NotNull;

public class DiInstantiationException extends RuntimeException {

	private final Class<?> wireType;

	public DiInstantiationException(@NotNull final String message, @NotNull Class<?> wireType, @NotNull Throwable e) {
		super(message, e);
		this.wireType = wireType;
	}

	public DiInstantiationException(@NotNull final String message, @NotNull Class<?> wireType) {
		super(message);
		this.wireType = wireType;
	}

	public Class<?> getWireType() {
		return wireType;
	}
}
