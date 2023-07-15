package com.wiredi.properties.keys;

import org.jetbrains.annotations.NotNull;

public interface Key {

	@NotNull
	String value();

	Key withPrefix(String prefix);

	static String joinWithSeparator(@NotNull String separator, @NotNull String prefix, @NotNull String suffix) {
		if (prefix.isBlank()) {
			return suffix;
		} else if (suffix.isBlank()) {
			return prefix;
		}

		if (prefix.endsWith(separator) && suffix.startsWith(separator)) {
			return prefix + suffix.substring(1);
		} else if (!prefix.endsWith(separator) && !suffix.startsWith(separator)) {
			return prefix + separator + suffix;
		} else {
			return prefix + suffix;
		}
	}

	static Key just(String value) {
		return new PreFormattedKey(value);
	}

	static Key format(String value) {
		return new CamelToKebabCaseKey(value);
	}
}
