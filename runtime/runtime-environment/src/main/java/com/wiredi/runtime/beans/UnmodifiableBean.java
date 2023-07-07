package com.wiredi.runtime.beans;

import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.domain.provider.TypeIdentifier;
import com.wiredi.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class UnmodifiableBean<T> extends AbstractBean<T> {

	private static final UnmodifiableBean<Object> EMPTY = new UnmodifiableBean<>(new HashMap<>(), new ArrayList<>(), TypeIdentifier.of(Object.class), null);

	public static <T> UnmodifiableBean<T> empty() {
		return (UnmodifiableBean<T>) EMPTY;
	}

	public UnmodifiableBean(
			@NotNull Map<QualifierType, IdentifiableProvider<T>> qualifiedProviders,
			@NotNull List<IdentifiableProvider<T>> unqualifiedProviders,
			@NotNull TypeIdentifier<T> typeIdentifier,
			@Nullable IdentifiableProvider<T> primary
	) {
		super(Collections.unmodifiableMap(qualifiedProviders), Collections.unmodifiableList(unqualifiedProviders), typeIdentifier);
		this.primary = primary;
	}

	@Override
	public void put(IdentifiableProvider<T> identifiableProvider) {
		throw new UnsupportedOperationException("put(IdentifiableProvider<T>)");
	}
}
