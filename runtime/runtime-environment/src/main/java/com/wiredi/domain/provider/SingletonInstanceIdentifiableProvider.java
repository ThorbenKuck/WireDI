package com.wiredi.domain.provider;

import com.wiredi.runtime.WireRepository;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class SingletonInstanceIdentifiableProvider<T> extends AbstractIdentifiableProvider<T> {

	@NotNull
	private final T instance;

	SingletonInstanceIdentifiableProvider(@NotNull final T instance) {
		super((TypeIdentifier<T>) TypeIdentifier.of(instance.getClass()));
		this.instance = instance;
	}

	<S extends T>SingletonInstanceIdentifiableProvider(@NotNull final S instance, TypeIdentifier<T> type) {
		super(type);
		this.instance = instance;
	}

	public static <T> SingletonInstanceIdentifiableProvider<T> of(@NotNull final T instance) {
		return new SingletonInstanceIdentifiableProvider<>(instance);
	}

	public static <T, S extends T> SingletonInstanceIdentifiableProvider<T> of(@NotNull final S instance, TypeIdentifier<T> type) {
		return new SingletonInstanceIdentifiableProvider<>(instance, type);
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

	@Override
	public String toString() {
		return "SingletonInstanceIdentifiableProvider{" + instance + '}';
	}
}