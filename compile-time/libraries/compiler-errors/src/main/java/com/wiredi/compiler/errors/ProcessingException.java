package com.wiredi.compiler.errors;

import javax.lang.model.element.Element;

public class ProcessingException extends RuntimeException {

	private final Element element;

	public ProcessingException(Element element, String msg) {
		super("[AT " + element.getSimpleName() + "]: " + msg);
		this.element = element;
	}

	public ProcessingException(Element element, String msg, Throwable cause) {
		super("[AT " + element.getSimpleName() + "]: " + msg, cause);
		this.element = element;
	}

	public Element getElement() {
		return element;
	}
}
