package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.domain.IdentifiableProvider;
import com.github.thorbenkuck.di.domain.WireRepository;

class RepositoryIdentifyingProvider implements IdentifiableProvider<WireRepository> {

	private final WiredTypes wiredTypes;
	private static final Class[] types = new Class[] {
			WireRepository.class,
			WiredTypes.class
	};

	RepositoryIdentifyingProvider(WiredTypes wiredTypes) {
		this.wiredTypes = wiredTypes;
	}

	@Override
	public Class type() {
		return WireRepository.class;
	}

	@Override
	public Class[] wiredTypes() {
		return types;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public WireRepository get(WireRepository wiredTypes) {
		return this.wiredTypes;
	}
}
