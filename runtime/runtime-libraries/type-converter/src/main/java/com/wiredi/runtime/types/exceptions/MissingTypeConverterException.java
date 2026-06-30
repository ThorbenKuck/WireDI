package com.wiredi.runtime.types.exceptions;

import com.wiredi.runtime.types.TypeConverter;

/**
 * Thrown when no {@link TypeConverter} is available for a requested conversion.
 *
 * This signals a configuration or capability gap where the system was asked to convert a value
 * to a specific type but there is no converter registered that can produce that target.
 */
public class MissingTypeConverterException extends RuntimeException {
	public MissingTypeConverterException(Class<?> type, String value) {
		super("Could not convert " + value + " to " + type + " because there is no TypeConverter registered");
	}
}
