package com.wiredi.runtime.properties.exceptions;

public class PropertyLoadingException extends RuntimeException {
	public PropertyLoadingException(String fileName, Throwable cause) {
		super("Failed to load the properties " + fileName, cause);
	}

	public PropertyLoadingException(String fileName, String reason) {
		super("Failed to load the properties " + fileName + ": " + reason);
	}
}
