package com.wiredi.properties.keys;

import org.jetbrains.annotations.NotNull;

public interface Key {

	@NotNull
	String value();

	static Key just(String value) {
		return new PreFormattedKey(value);
	}

	static Key format(String value) {
		return new CamelToKebabCaseKey(value);
	}
}
