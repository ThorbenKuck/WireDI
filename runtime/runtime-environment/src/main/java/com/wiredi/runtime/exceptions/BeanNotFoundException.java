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
	private final WireContainer wireRepository;

	public <T>BeanNotFoundException(
			@NotNull TypeIdentifier<T> typeIdentifier,
			@NotNull WireContainer wireRepository
	) {
		super("Could not find a bean for the type " + typeIdentifier);
		this.typeIdentifier = typeIdentifier;
		this.wireRepository = wireRepository;
		this.qualifierType = null;
	}

	public <T>BeanNotFoundException(
			@NotNull TypeIdentifier<T> typeIdentifier,
			@Nullable QualifierType qualifierType,
			@NotNull WireContainer wireRepository
	) {
		super(constructMessage(typeIdentifier, qualifierType));
		this.typeIdentifier = typeIdentifier;
		this.qualifierType = qualifierType;
		this.wireRepository = wireRepository;
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

	public WireContainer wireRepository() {
		return wireRepository;
	}
}
