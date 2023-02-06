package com.github.thorbenkuck.di.domain.provider;

import com.github.thorbenkuck.di.runtime.WireRepository;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

class SingletonInstanceIdentifiableProvider<T> implements IdentifiableProvider<T> {

	@NotNull
	private final T instance;
	@NotNull
	private final TypeIdentifier<?>[] types;
	@NotNull
	private final Class<?> type;

	SingletonInstanceIdentifiableProvider(@NotNull final T instance) {
		this.instance = instance;
		this.type = instance.getClass();
		this.types = new TypeIdentifier[]{TypeIdentifier.of(instance.getClass())};
	}

	SingletonInstanceIdentifiableProvider(@NotNull final T instance, Class<?>... classes) {
		this.instance = instance;
		this.type = instance.getClass();
		this.types = Arrays.stream(classes)
				.map(TypeIdentifier::of)
				.collect(Collectors.toList())
				.toArray(new TypeIdentifier[]{});
	}

	@Override
	@NotNull
	public Class<?> type() {
		return type;
	}

	@Override
	@NotNull
	public TypeIdentifier<?>[] wiredTypes() {
		return types;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	@NotNull
	public T get(@NotNull final WireRepository wiredRepository) {
		return instance;
	}
}