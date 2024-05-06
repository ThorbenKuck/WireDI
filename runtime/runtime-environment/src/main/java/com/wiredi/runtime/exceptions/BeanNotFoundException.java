package com.wiredi.runtime.exceptions;

import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.qualifier.QualifierType;
import com.wiredi.runtime.WireRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BeanNotFoundException extends RuntimeException {

	@NotNull
	private final TypeIdentifier<?> typeIdentifier;

	@Nullable
	private final QualifierType qualifierType;

	@NotNull
	private final WireRepository wireRepository;

	public <T>BeanNotFoundException(
			@NotNull TypeIdentifier<T> typeIdentifier,
			@NotNull WireRepository wireRepository
	) {
		super("Could not find a bean for the type " + typeIdentifier);
		this.typeIdentifier = typeIdentifier;
		this.wireRepository = wireRepository;
		this.qualifierType = null;
	}

	public <T>BeanNotFoundException(
			@NotNull TypeIdentifier<T> typeIdentifier,
			@Nullable QualifierType qualifierType,
			@NotNull WireRepository wireRepository
	) {
		super("Could not find a bean for the type " + typeIdentifier + " with qualifier " + qualifierType);
		this.typeIdentifier = typeIdentifier;
		this.qualifierType = qualifierType;
		this.wireRepository = wireRepository;
	}

	public TypeIdentifier<?> getTypeIdentifier() {
		return typeIdentifier;
	}

	public QualifierType getQualifierType() {
		return qualifierType;
	}

	public WireRepository wireRepository() {
		return wireRepository;
	}
}
