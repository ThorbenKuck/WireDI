package com.github.thorbenkuck.di;

public class DiLoadingException extends RuntimeException {

	public DiLoadingException(String message) {
		super(message);
	}

	public DiLoadingException(String message, Throwable cause) {
		super(message, cause);
	}
}
