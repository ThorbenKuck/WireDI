package com.github.thorbenkuck.di.annotations;

public enum Origin {
	/**
	 * This Origin means, that the generated ResourceProvider
	 * should use.
	 * <code>
	 * String value = System.getProperty(name);
	 * </code>
	 */
	SYSTEM_PROPERTIES,
	/**
	 * This Origin means, that the generated class should
	 * not use any custom means to find the property.
	 * <p>
	 * This is mostly useless now. I get it. It might be used,
	 * to create resources, that are only assigned by their
	 * default value.
	 * <p>
	 * Another potential use is, to let the user manually inject the variable.
	 * In this case, there should be no generated ResourceProvider,
	 * but the injection should happen anyways.
	 */
	NONE;
}