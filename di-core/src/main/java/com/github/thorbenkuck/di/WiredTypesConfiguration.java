package com.github.thorbenkuck.di;

import com.github.thorbenkuck.di.annotations.ManualWireCandidate;
import org.jetbrains.annotations.NotNull;

@ManualWireCandidate
public final class WiredTypesConfiguration {

	@NotNull
	private WireConflictStrategy wireConflictStrategy = WireConflictStrategy.valueOf(
			System.getProperty("simple.di.wired.concurrent-definition-strategy", WireConflictStrategy.DEFAULT.toString())
	);

	public boolean doDiAutoLoad() {
		return toBoolean("simple.di.wired.autoload", true);
	}

	public void setWireConflictStrategy(@NotNull final WireConflictStrategy strategy) {
		wireConflictStrategy = strategy;
	}

	public static void globallySetWireConflictStrategy(@NotNull final WireConflictStrategy strategy) {
		System.setProperty("simple.di.wired.concurrent-definition-strategy", strategy.name());
	}

	public WireConflictStrategy conflictStrategy() {
		return wireConflictStrategy;
	}

	private static boolean toBoolean(
			@NotNull final String string,
			final boolean defaultValue
	) {
		final String property = System.getProperty(string);
		if(property == null) {
			return defaultValue;
		}

		return Boolean.parseBoolean(property);
	}
}
