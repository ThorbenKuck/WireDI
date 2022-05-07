package com.github.thorbenkuck.di.processor;

import com.github.thorbenkuck.di.runtime.properties.TypedProperties;

import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;

public class ProcessorProperties {

	private static final String FILE_NAME = "wire-di.processor.properties";
	private static final TypedProperties PROPERTIES;

	static {
		InputStream inputStream = null;
		if(ProcessorContext.isFilerSet()) {
			try {
				inputStream = ProcessorContext.getFiler().getResource(StandardLocation.CLASS_PATH, "", FILE_NAME).openInputStream();
			} catch (IOException ignored) {
			}
		}

		if(inputStream == null) {
			PROPERTIES = new TypedProperties();
		} else {
			PROPERTIES = TypedProperties.fromInputStreamOrEmpty(inputStream);
		}

		for (PropertyKeys value : PropertyKeys.values()) {
			PROPERTIES.tryTakeFromEnvironment(value.getRawKey(), value.getDefaultValue());
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

	public static String getName(PropertyKeys extensionFileName) {
		return PROPERTIES.get(extensionFileName.getRawKey());
	}

	public static void updateBy(InputStream inputStream) {
		if (inputStream == null) {
			return;
		}
		try {
			PROPERTIES.loadProperties(inputStream);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
