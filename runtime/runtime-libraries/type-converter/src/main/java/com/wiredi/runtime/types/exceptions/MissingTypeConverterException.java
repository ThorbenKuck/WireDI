package com.wiredi.runtime.types.exceptions;

import com.wiredi.runtime.types.TypeConverter;

/**
 * This exception indicates that no {@link TypeConverter}
 * was found for the {@code value}
 */
public class MissingTypeConverterException extends RuntimeException {
	public MissingTypeConverterException(Class<?> type, String value) {
		super("Could not convert " + value + " to " + type + " because there is no TypeConverter registered");
	}
}
