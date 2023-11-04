package com.wiredi.runtime.beans;

import com.wiredi.domain.WireConflictResolver;
import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.domain.provider.TypeIdentifier;
import com.wiredi.qualifier.QualifierType;

import java.util.*;
import java.util.function.Supplier;

public interface Bean<T> {

	static <T> Bean<T> empty() {
		return UnmodifiableBean.empty();
	}

	List<IdentifiableProvider<T>> getAll();

	List<IdentifiableProvider<T>> getAllUnqualified();

	List<IdentifiableProvider<T>> getAllQualified();

	Optional<IdentifiableProvider<T>> get(QualifierType qualifierType);

	Optional<IdentifiableProvider<T>> get(TypeIdentifier<T> concreteType, Supplier<WireConflictResolver> conflictResolver);

	boolean isEmpty();

	default boolean isNotEmpty() {
		return !isEmpty();
	}
}
