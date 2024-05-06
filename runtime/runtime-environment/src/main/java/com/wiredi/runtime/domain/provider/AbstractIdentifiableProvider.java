package com.wiredi.runtime.domain.provider;

import com.wiredi.runtime.domain.provider.condition.LoadCondition;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractIdentifiableProvider<T> implements IdentifiableProvider<T> {

	private final TypeIdentifier<T> typeIdentifier;
	private final List<TypeIdentifier<?>> additionalTypeIdentifiers = new ArrayList<>();
	private final List<QualifierType> qualifiers = new ArrayList<>();
	private LoadCondition condition = LoadCondition.TRUE;

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
	public @NotNull List<QualifierType> qualifiers() {
		return qualifiers;
	}

	@Override
	public @NotNull TypeIdentifier<T> type() {
		return typeIdentifier;
	}

	@Override
	public @Nullable LoadCondition condition() {
		return condition;
	}

	public AbstractIdentifiableProvider<T> withAdditionalTypeIdentifier(TypeIdentifier<?> identifier) {
		additionalTypeIdentifiers.add(identifier);
		return this;
	}

	public AbstractIdentifiableProvider<T> withQualifier(QualifierType qualifierType) {
		this.qualifiers.add(qualifierType);
		return this;
	}

	public AbstractIdentifiableProvider<T> withLoadCondition(LoadCondition loadCondition) {
		this.condition = loadCondition;
		return this;
	}
}
