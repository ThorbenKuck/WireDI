package com.wiredi.runtime.beans;

import com.wiredi.domain.WireConflictResolver;
import com.wiredi.domain.provider.IdentifiableProvider;
import com.wiredi.domain.provider.TypeIdentifier;
import com.wiredi.qualifier.QualifierType;
import com.wiredi.runtime.exceptions.DiLoadingException;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class AbstractBean<T> implements Bean<T> {

	private final Map<QualifierType, IdentifiableProvider<T>> qualifiedProviders;
	private final List<IdentifiableProvider<T>> unqualifiedProviders;
	private final TypeIdentifier<T> typeIdentifier;

	@Nullable
	protected IdentifiableProvider<T> primary;

	protected AbstractBean(TypeIdentifier<T> typeIdentifier) {
		this(new HashMap<>(), new ArrayList<>(), typeIdentifier);
	}

	protected AbstractBean(
			Map<QualifierType, IdentifiableProvider<T>> qualifiedProviders,
			List<IdentifiableProvider<T>> unqualifiedProviders,
			TypeIdentifier<T> typeIdentifier
	) {
		this.qualifiedProviders = qualifiedProviders;
		this.unqualifiedProviders = unqualifiedProviders;
		this.typeIdentifier = typeIdentifier;
	}

	@Override
	public void put(IdentifiableProvider<T> identifiableProvider) {
		if (identifiableProvider.primary()) {
			if (primary != null) {
				throw new DiLoadingException(
						"Multiple primary provider found." + System.lineSeparator()
								+ " - " + primary + System.lineSeparator()
								+ " - " + identifiableProvider
				);
			}
			primary = identifiableProvider;
		}
		if (identifiableProvider.qualifiers().isEmpty()) {
			unqualifiedProviders.add(identifiableProvider);
		} else {
			identifiableProvider.qualifiers().forEach(qualifier -> qualifiedProviders.put(qualifier, identifiableProvider));
		}
	}

	@Override
	public List<IdentifiableProvider<T>> getAll() {
		ArrayList<IdentifiableProvider<T>> result = new ArrayList<>(unqualifiedProviders);
		result.addAll(qualifiedProviders.values());
		return result;
	}

	@Override
	public Optional<IdentifiableProvider<T>> get(QualifierType qualifierType) {
		return Optional.ofNullable(qualifiedProviders.get(qualifierType));
	}

	@Override
	public Optional<IdentifiableProvider<T>> get(WireConflictResolver conflictResolver) {
		if (primary != null) {
			return Optional.of(primary);
		} else if (unqualifiedProviders.size() == 1) {
			return Optional.ofNullable(unqualifiedProviders.get(0));
		}
		List<IdentifiableProvider<T>> all = getAll();
		if (all.isEmpty()) {
			return Optional.empty();
		}
		if (all.size() > 1) {
			IdentifiableProvider<T> provider = conflictResolver.find(all, typeIdentifier);
			return Optional.of(provider);
		}
		return Optional.of(all.get(0));
	}
}
