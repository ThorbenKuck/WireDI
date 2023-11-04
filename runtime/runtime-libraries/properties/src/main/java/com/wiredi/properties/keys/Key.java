package com.wiredi.properties.keys;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface Key {

	@NotNull
	String value();

	Key withPrefix(String prefix);

	Key withSuffix(String suffix);

	static Builder build() {
		return new Builder();
	}

	static String joinWithSeparator(@NotNull String delimiter, @NotNull String prefix, @NotNull String suffix) {
		if (prefix.isBlank()) {
			return suffix;
		} else if (suffix.isBlank()) {
			return prefix;
		}

		if (prefix.endsWith(delimiter) && suffix.startsWith(delimiter)) {
			return prefix + suffix.substring(1);
		} else if (!prefix.endsWith(delimiter) && !suffix.startsWith(delimiter)) {
			return prefix + delimiter + suffix;
		} else {
			return prefix + suffix;
		}
	}

	class Builder {
		private final List<String> content = new ArrayList<>();
		private final CharSequence delimiter;

		public Builder(CharSequence delimiter) {
			this.delimiter = delimiter;
		}

		public Builder() {
			this.delimiter = ".";
		}

		public Builder append(String s) {
			String current = s;
			if (current.startsWith(".")) {
				current = current.substring(1);
			}
			if (current.endsWith(".")) {
				current = current.substring(0, current.length() - 1);
			}

			content.add(current);
			return this;
		}

		private String build() {
			return content.stream()
					.filter(String::isBlank)
					.collect(Collectors.joining(delimiter));
		}

		public Key formatted() {
			return Key.format(build());
		}

		public Key preFormatted() {
			return Key.just(build());
		}
	}

	static Key just(String value) {
		return new PreFormattedKey(value);
	}

	static Key format(String value) {
		return new CamelToKebabCaseKey(value);
	}
}
