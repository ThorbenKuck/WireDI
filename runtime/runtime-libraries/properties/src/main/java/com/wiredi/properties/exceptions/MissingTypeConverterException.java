package com.wiredi.properties.exceptions;

public class MissingTypeConverterException extends RuntimeException {
	public MissingTypeConverterException(Class<?> type, String value) {
		super("Could not convert " + value + " to " + type + " because there is no TypeConverter registered");
	}
}
