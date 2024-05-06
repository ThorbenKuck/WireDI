package com.wiredi.runtime.domain.provider;

import com.wiredi.runtime.WireRepository;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class LazySingletonIdentifiableProvider<T> extends AbstractIdentifiableProvider<T> {

	@NotNull
	private final Function<WireRepository, T> creationFunction;

	private volatile T instance;

	public LazySingletonIdentifiableProvider(
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
	public synchronized T get(
			@NotNull final WireRepository wireRepository,
			@NotNull final TypeIdentifier<T> concreteType
	) {
		if(instance == null) {
			instance = creationFunction.apply(wireRepository);
		}

		return instance;
	}
}