package com.wiredi.domain.provider;

import com.wiredi.runtime.WireRepository;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

class SingletonGenericIdentifiableProvider<T> extends AbstractIdentifiableProvider<T> {

	@NotNull
	private final Function<WireRepository, T> creationFunction;

	private volatile T instance;

	SingletonGenericIdentifiableProvider(
			@NotNull final Function<WireRepository, T> creationFunction,
			@NotNull List<TypeIdentifier<?>> wireTypes,
			@NotNull TypeIdentifier<T> type
	) {
		super(type, wireTypes);
		this.creationFunction = creationFunction;
	}

	@Override
	public final boolean isSingleton() {
		return true;
	}

	@Override
	@NotNull
	public synchronized T get(@NotNull final WireRepository wiredRepository) {
		if(instance == null) {
			instance = creationFunction.apply(wiredRepository);
		}

		return instance;
	}
}