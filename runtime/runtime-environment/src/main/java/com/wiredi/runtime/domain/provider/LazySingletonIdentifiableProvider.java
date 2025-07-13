package com.wiredi.runtime.domain.provider;

import com.wiredi.runtime.WireContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class LazySingletonIdentifiableProvider<T> extends AbstractIdentifiableProvider<T> {

	@NotNull
	private final Function<WireContainer, T> creationFunction;

	private volatile T instance;

	public LazySingletonIdentifiableProvider(
			@NotNull final Function<WireContainer, T> creationFunction,
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
	public synchronized T get(
			@NotNull final WireContainer wireRepository,
			@NotNull final TypeIdentifier<T> concreteType
	) {
		if(instance == null) {
			instance = creationFunction.apply(wireRepository);
		}

		return instance;
	}
}