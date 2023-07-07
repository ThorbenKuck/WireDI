package com.wiredi.processor.tck.domain.ordered;

public class CommandContext {

	private StringBuilder builder = new StringBuilder();

	public void append(String t) {
		this.builder.append(t);
	}

	public String stringify() {
		return builder.toString();
	}
}
