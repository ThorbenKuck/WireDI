package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.domain.IdentifiableProvider;
import com.github.thorbenkuck.di.domain.WireRepository;
import org.jetbrains.annotations.NotNull;

final class RepositoryIdentifyingProvider implements IdentifiableProvider<WireRepository> {

	@NotNull
	private final WiredTypes wiredTypes;

	@NotNull
	private static final Class<?>[] types = new Class[] {
			WireRepository.class,
			WiredTypes.class
	};

	RepositoryIdentifyingProvider(@NotNull final WiredTypes wiredTypes) {
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
		return types;
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
