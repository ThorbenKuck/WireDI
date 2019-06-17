package com.github.thorbenkuck.di;

public class Configuration {

	private static boolean toBoolean(String string) {
		// Copy the instance to
		// not override the argument.
		// This should never matter
		// because the String is immutable.
		// We don't know yet, whether
		// we not the original value
		// later or not. Therefore we
		// copy the value here.
		String value = string;
		if(value == null) {
			value = "true";
		}

		return Boolean.parseBoolean(value);
	}

	public static boolean doDiAutoLoad() {
		String autoLoad = System.getProperty("di.simple.autoload");

		return toBoolean(autoLoad);
	}

	public static boolean doResourceAutoLoad() {
		String autoLoad = System.getProperty("di.simple.resources.autoload");

		return toBoolean(autoLoad);
	}

}
