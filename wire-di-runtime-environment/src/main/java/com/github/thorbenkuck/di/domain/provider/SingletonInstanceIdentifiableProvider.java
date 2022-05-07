package com.github.thorbenkuck.di.domain.provider;

import com.github.thorbenkuck.di.runtime.WireRepository;
import org.jetbrains.annotations.NotNull;

class SingletonInstanceIdentifiableProvider<T> implements IdentifiableProvider<T> {

	@NotNull
	private final T instance;
	@NotNull
	private final Class<?>[] types;
	@NotNull
	private final Class<?> type;

	SingletonInstanceIdentifiableProvider(@NotNull final T instance) {
		this.instance = instance;
		this.type = instance.getClass();
		this.types = new Class[]{instance.getClass()};
	}

	SingletonInstanceIdentifiableProvider(@NotNull final T instance, Class<?>... classes) {
		this.instance = instance;
		this.type = instance.getClass();
		this.types = classes;
	}

	@Override
	@NotNull
	public Class<?> type() {
		return type;
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
	public T get(@NotNull final WireRepository wiredRepository) {
		return instance;
	}
}