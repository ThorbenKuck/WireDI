package com.github.thorbenkuck.di.processor;

import com.github.thorbenkuck.di.properties.TypedProperties;

public class ProcessorProperties {

	private static final TypedProperties PROPERTIES = TypedProperties.fromClassPathOrEmpty("wire-di.processor.properties");

	static {
		for (PropertyKeys value : PropertyKeys.values()) {
			PROPERTIES.tryTakeFromEnvironment(value.getRawKey(), value.getDefaultValue());
		}

		if (!PROPERTIES.getBoolean(PropertyKeys.WARN_REFLECTION_USAGE.getRawKey())) {
			Logger.warn("You have disabled warnings on reflection. It is understandable that the warnings are annoying, but there are different approaches than just disabling the warnings. Maybe take a look at those!");
		}
	}

	public static boolean isEnabled(PropertyKeys propertyKey) {
		return PROPERTIES.getBoolean(propertyKey.getRawKey());
	}

	public static boolean isDisabled(PropertyKeys propertyKey) {
		return !isEnabled(propertyKey);
	}

	public static int getCount(PropertyKeys propertyKey) {
		return PROPERTIES.getInt(propertyKey.getRawKey());
	}
}
