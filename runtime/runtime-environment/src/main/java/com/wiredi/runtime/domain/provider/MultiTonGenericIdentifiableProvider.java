package com.wiredi.runtime.domain.provider;

import com.wiredi.runtime.WireRepository;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class MultiTonGenericIdentifiableProvider<T> extends AbstractIdentifiableProvider<T> {

	@NotNull
	private final Function<WireRepository, T> creationFunction;

	public MultiTonGenericIdentifiableProvider(
			@NotNull final Function<WireRepository, T> creationFunction,
			@NotNull List<TypeIdentifier<?>> wireTypes,
			@NotNull TypeIdentifier<T> type
	) {
		super(type, wireTypes);
		this.creationFunction = creationFunction;
	}

	@Override
	public final boolean isSingleton() {
		return false;
	}

	@Override
	@NotNull
	public T get(
			@NotNull final WireRepository wireRepository,
			@NotNull final TypeIdentifier<T> concreteType
			) {
		return creationFunction.apply(wireRepository);
	}
}