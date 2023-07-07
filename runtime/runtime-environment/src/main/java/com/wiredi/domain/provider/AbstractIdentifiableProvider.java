package com.wiredi.domain.provider;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractIdentifiableProvider<T> implements IdentifiableProvider<T> {

	private final TypeIdentifier<T> typeIdentifier;
	private final List<TypeIdentifier<?>> additionalTypeIdentifiers = new ArrayList<>();

	public AbstractIdentifiableProvider(TypeIdentifier<T> typeIdentifier) {
		this(typeIdentifier, new ArrayList<>());
	}

	public AbstractIdentifiableProvider(
			TypeIdentifier<T> typeIdentifier,
			List<TypeIdentifier<?>> additionalTypeIdentifiers
	) {
		this.typeIdentifier = typeIdentifier;
		this.additionalTypeIdentifiers.addAll(additionalTypeIdentifiers);
	}

	@Override
	public @NotNull List<TypeIdentifier<?>> additionalWireTypes() {
		return additionalTypeIdentifiers;
	}

	@Override
	public @NotNull TypeIdentifier<T> type() {
		return typeIdentifier;
	}
}
