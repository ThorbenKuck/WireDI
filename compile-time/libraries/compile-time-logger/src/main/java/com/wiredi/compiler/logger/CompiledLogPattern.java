package com.wiredi.compiler.logger;

import java.util.List;

public record CompiledLogPattern(String layout, List<Object> arguments) {
	public String format() {
		return String.format(layout, arguments.toArray());
	}
}
