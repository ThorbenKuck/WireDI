package com.wiredi.runtime.exceptions;

import com.wiredi.domain.provider.TypeIdentifier;
import com.wiredi.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BeanNotFoundException extends RuntimeException {

	@NotNull
	private final TypeIdentifier<?> typeIdentifier;

	@Nullable
	private final QualifierType qualifierType;

	public <T>BeanNotFoundException(@NotNull TypeIdentifier<T> typeIdentifier) {
		super("Could not find a bean for the type " + typeIdentifier);
		this.typeIdentifier = typeIdentifier;
		this.qualifierType = null;
	}

	public <T>BeanNotFoundException(
			@NotNull TypeIdentifier<T> typeIdentifier,
			@Nullable QualifierType qualifierType
	) {
		super("Could not find a bean for the type " + typeIdentifier + " with qualifier " + qualifierType);
		this.typeIdentifier = typeIdentifier;
		this.qualifierType = qualifierType;
	}

	public TypeIdentifier<?> getTypeIdentifier() {
		return typeIdentifier;
	}

	public QualifierType getQualifierType() {
		return qualifierType;
	}
}
