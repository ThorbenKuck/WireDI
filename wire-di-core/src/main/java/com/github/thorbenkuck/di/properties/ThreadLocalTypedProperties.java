package com.github.thorbenkuck.di.properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ThreadLocalTypedProperties extends ThreadLocal<TypedProperties> {

	@NotNull
	private final Supplier<TypedProperties> supplier;
	private final boolean copyInstance;

	public ThreadLocalTypedProperties(@NotNull Supplier<@Nullable TypedProperties> supplier, boolean copyInstance) {
		this.supplier = supplier;
		this.copyInstance = copyInstance;
	}

	public ThreadLocalTypedProperties(@NotNull Supplier<@Nullable TypedProperties> supplier) {
		this(supplier, false);
	}

	public ThreadLocalTypedProperties(@Nullable TypedProperties base) {
		this(() -> base, true);
	}

	public ThreadLocalTypedProperties() {
		this(() -> null, false);
	}

	@Override
	protected TypedProperties initialValue() {
		TypedProperties base = supplier.get();
		if(base == null) {
			return null;
		}

		if(copyInstance) {
			return base.copy();
		} else {
			return base;
		}
	}
}
