package com.github.thorbenkuck.di.processor;

import javax.lang.model.element.Element;

public class ProcessingException extends RuntimeException {

	private final Element element;

	public ProcessingException(Element element, String msg) {
		super(msg);
		this.element = element;
	}

	public Element getElement() {
		return element;
	}
}
