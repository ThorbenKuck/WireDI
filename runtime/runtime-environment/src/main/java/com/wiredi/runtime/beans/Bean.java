package com.wiredi.runtime.beans;

import com.wiredi.domain.WireConflictResolver;
import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.qualifier.QualifierType;

import java.util.*;

public interface Bean<T> {

	static <T> Bean<T> empty() {
		return UnmodifiableBean.empty();
	}

	void put(IdentifiableProvider<T> identifiableProvider);

	List<IdentifiableProvider<T>> getAll();

	Optional<IdentifiableProvider<T>> get(QualifierType qualifierType);

	Optional<IdentifiableProvider<T>> get(WireConflictResolver conflictResolver);
}
