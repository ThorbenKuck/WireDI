package com.wiredi.processor.processors.adapter;

import com.wiredi.annotations.Wire;

public class WireAnnotationContext {

	private static final ThreadLocal<Wire> content = new ThreadLocal<>();

	public Wire current() {
		return content.get();
	}

	public void set(Wire wire) {
		content.set(wire);
	}

	public void clear() {
		content.remove();
	}
}
