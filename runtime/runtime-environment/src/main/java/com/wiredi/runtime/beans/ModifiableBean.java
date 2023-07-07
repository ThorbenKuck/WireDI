package com.wiredi.runtime.beans;

import com.wiredi.domain.provider.TypeIdentifier;

public class ModifiableBean<T> extends AbstractBean<T> {

	public static final ModifiableBean<?> EMPTY = new ModifiableBean<>(TypeIdentifier.of(Void.class));

	public ModifiableBean(TypeIdentifier<T> typeIdentifier) {
		super(typeIdentifier);
	}

	public static <T> ModifiableBean<T> empty() {
		return (ModifiableBean<T>) EMPTY;
	}
}
