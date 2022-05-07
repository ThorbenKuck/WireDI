package com.github.thorbenkuck.di.domain.provider;

import com.github.thorbenkuck.di.runtime.WireRepository;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

class MultitonGenericIdentifiableProvider<T> implements IdentifiableProvider<T> {

	@NotNull
	private final Function<WireRepository, T> creationFunction;
	@NotNull
	private final Class<?>[] wireTypes;
	@NotNull
	private final Class<T> type;

	MultitonGenericIdentifiableProvider(
			@NotNull final Function<WireRepository, T> creationFunction,
			@NotNull Class<?>[] wireTypes,
			@NotNull Class<T> type
	) {
		this.creationFunction = creationFunction;
		this.type = type;
		this.wireTypes = wireTypes;
	}

	@Override
	@NotNull
	public final Class<?> type() {
		return type;
	}

	@Override
	@NotNull
	public final Class<?>[] wiredTypes() {
		return wireTypes;
	}

	@Override
	public final boolean isSingleton() {
		return false;
	}

	@Override
	@NotNull
	public T get(@NotNull final WireRepository wiredRepository) {
		return creationFunction.apply(wiredRepository);
	}
}