package com.github.thorbenkuck.di;

public class DiInstantiationException extends RuntimeException {

	public DiInstantiationException(String message) {
		super(message);
	}

	public DiInstantiationException(String message, Throwable cause) {
		super(message, cause);
	}
}
