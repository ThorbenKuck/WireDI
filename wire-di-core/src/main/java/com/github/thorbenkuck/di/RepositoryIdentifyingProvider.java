package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.domain.IdentifiableProvider;
import org.jetbrains.annotations.NotNull;

final class RepositoryIdentifyingProvider implements IdentifiableProvider<WireRepository> {

	@NotNull
	private final WireRepository wiredTypes;

	@NotNull
	private static final Class<?>[] TYPES = new Class[] { WireRepository.class };

	RepositoryIdentifyingProvider(@NotNull final WireRepository wiredTypes) {
		this.wiredTypes = wiredTypes;
	}

	@Override
	@NotNull
	public Class<?> type() {
		return WireRepository.class;
	}

	@Override
	@NotNull
	public Class<?>[] wiredTypes() {
		return TYPES;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	@NotNull
	public WireRepository get(@NotNull final WireRepository wiredTypes) {
		return this.wiredTypes;
	}
}
