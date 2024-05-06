package com.wiredi.runtime.beans;

import com.wiredi.runtime.domain.provider.IdentifiableProvider;
import com.wiredi.runtime.domain.provider.TypeIdentifier;
import com.wiredi.runtime.qualifier.QualifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class UnmodifiableBean<T> extends AbstractBean<T> {

	private static final UnmodifiableBean<Object> EMPTY = new UnmodifiableBean<>(Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), TypeIdentifier.OBJECT, null);

	public static <T> UnmodifiableBean<T> empty() {
		return (UnmodifiableBean<T>) EMPTY;
	}

	public UnmodifiableBean(
			@NotNull Map<QualifierType, IdentifiableProvider<T>> qualifiedProviders,
			@NotNull Map<TypeIdentifier<T>, TypedProviderState<T>> typedProviders,
			@NotNull Set<IdentifiableProvider<T>> unqualifiedProviders,
			@NotNull TypeIdentifier<T> rootType,
			@Nullable IdentifiableProvider<T> primary
	) {
		super(Map.copyOf(qualifiedProviders), Map.copyOf(typedProviders), Set.copyOf(unqualifiedProviders), rootType);
		this.primary = primary;
	}
}
