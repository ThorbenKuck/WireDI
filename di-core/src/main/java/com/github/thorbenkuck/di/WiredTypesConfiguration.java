package com.github.thorbenkuck.di;

public class WiredTypesConfiguration {

	private WireConflictStrategy wireConflictStrategy = WireConflictStrategy.valueOf(
			System.getProperty("simple.di.wired.concurrent-definition-strategy", WireConflictStrategy.DEFAULT.toString())
	);

	public boolean doDiAutoLoad() {
		return toBoolean("simple.di.wired.autoload", true);
	}

	public void setWireConflictStrategy(WireConflictStrategy strategy) {
		wireConflictStrategy = strategy;
	}

	public static void globallySetWireConflictStrategy(WireConflictStrategy strategy) {
		System.setProperty("simple.di.wired.concurrent-definition-strategy", strategy.name());
	}

	public WireConflictStrategy conflictStrategy() {
		return wireConflictStrategy;
	}

	private static boolean toBoolean(String string, boolean defaultValue) {
		String property = System.getProperty(string);
		if(property == null) {
			return defaultValue;
		}

		return Boolean.parseBoolean(property);
	}
}
