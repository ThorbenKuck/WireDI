package com.wiredi.runtime.exceptions;

import com.wiredi.runtime.WireContainer;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BeanNotFoundException extends RuntimeException {

	@NotNull
	private final TypeIdentifier<?> typeIdentifier;

	@Nullable
	private final QualifierType qualifierType;

	@NotNull
	private final WireContainer wireContainer;

	public <T>BeanNotFoundException(
			@NotNull TypeIdentifier<T> typeIdentifier,
			@NotNull WireContainer wireContainer
	) {
		super("Could not find a bean for the type " + typeIdentifier);
		this.typeIdentifier = typeIdentifier;
		this.wireContainer = wireContainer;
		this.qualifierType = null;
	}

	public <T>BeanNotFoundException(
			@NotNull TypeIdentifier<T> typeIdentifier,
			@Nullable QualifierType qualifierType,
			@NotNull WireContainer wireContainer
	) {
		super(constructMessage(typeIdentifier, qualifierType));
		this.typeIdentifier = typeIdentifier;
		this.qualifierType = qualifierType;
		this.wireContainer = wireContainer;
	}

	private static String constructMessage(TypeIdentifier<?> typeIdentifier, QualifierType qualifierType) {
		if (qualifierType == null) {
			return "Could not find a bean for the type " + typeIdentifier;
		} else {
			return "Could not find a bean for the type " + typeIdentifier + " with qualifier " + qualifierType;
		}
	}

	public TypeIdentifier<?> getTypeIdentifier() {
		return typeIdentifier;
	}

	public QualifierType getQualifierType() {
		return qualifierType;
	}

	public WireContainer wireContainer() {
		return wireContainer;
	}
}
