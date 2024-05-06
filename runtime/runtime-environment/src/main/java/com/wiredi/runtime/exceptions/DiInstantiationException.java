package com.wiredi.runtime.exceptions;

import com.wiredi.runtime.domain.provider.TypeIdentifier;
import org.jetbrains.annotations.NotNull;

public class DiInstantiationException extends RuntimeException {

	private final TypeIdentifier<?> wireType;

	public DiInstantiationException(@NotNull final String message, @NotNull TypeIdentifier<?> wireType, @NotNull Throwable e) {
		super(message, e);
		this.wireType = wireType;
	}

	public DiInstantiationException(@NotNull final String message, @NotNull TypeIdentifier<?> wireType) {
		super(message);
		this.wireType = wireType;
	}

	public DiInstantiationException(@NotNull final Throwable cause, @NotNull TypeIdentifier<?> wireType) {
		super(cause);
		this.wireType = wireType;
	}

	public TypeIdentifier<?> getWireType() {
		return wireType;
	}
}
