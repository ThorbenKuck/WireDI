package com.wiredi.runtime.properties.exceptions;

import com.wiredi.runtime.properties.PropertyConverter;

/**
 * This exception indicates that no {@link PropertyConverter}
 * was found for the {@code value}
 */
public class MissingTypeConverterException extends RuntimeException {
	public MissingTypeConverterException(Class<?> type, String value) {
		super("Could not convert " + value + " to " + type + " because there is no TypeConverter registered");
	}
}
