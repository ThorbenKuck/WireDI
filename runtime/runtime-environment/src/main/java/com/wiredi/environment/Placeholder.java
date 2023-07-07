package com.wiredi.environment;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class Placeholder {

	private final String start;
	private final String stop;
	private final String input;
	private final String placeholderValue;
	@Nullable private final Placeholder.Default defaultValue;
	private final char identifierChar;

	public Placeholder(
			String start,
			String stop,
			String input,
			String placeholderValue,
			@Nullable Placeholder.Default defaultValue,
			char identifierChar
	) {
		this.start = start;
		this.stop = stop;
		this.input = input;
		if (defaultValue != null) {
			this.placeholderValue = placeholderValue.replaceFirst(Pattern.quote(defaultValue.toString()), "");
		} else {
			this.placeholderValue = placeholderValue;
		}
		this.defaultValue = defaultValue;
		this.identifierChar = identifierChar;
	}

	public char getIdentifierChar() {
		return identifierChar;
	}

	public String originalValue() {
		StringBuilder stringBuilder = new StringBuilder()
				.append(identifierChar)
				.append(start)
				.append(placeholderValue);

		if (defaultValue != null) {
			stringBuilder.append(defaultValue);
		}

		return stringBuilder.append(stop).toString();
	}

	public String getPlaceholderValue() {
		return placeholderValue;
	}

	public String replaceIn(String value, String replacement) {
		return value.replace(originalValue(), replacement);
	}

	@Nullable
	public String tryReplacementWithDefault(String value) {
		return Optional.ofNullable(defaultValue)
				.map(it -> replaceIn(value, it.replacement))
				.orElse(null);
	}

	public String replaceWith(String replacement) {
		return replaceIn(input, replacement);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Placeholder that = (Placeholder) o;
		return Objects.equals(start, that.start) &&
				Objects.equals(stop, that.stop) &&
				identifierChar == that.identifierChar &&
				Objects.equals(input, that.input) &&
				Objects.equals(placeholderValue, that.placeholderValue) &&
				Objects.equals(defaultValue, that.defaultValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, stop, input, placeholderValue, identifierChar, defaultValue);
	}

	@Override
	public String toString() {
		return "Placeholder{" +
				"start='" + start + '\'' +
				", stop='" + stop + '\'' +
				", input='" + input + '\'' +
				", defaultValue='" + defaultValue + '\'' +
				", placeholderValue='" + placeholderValue + '\'' +
				", identifierChar=" + identifierChar +
				'}';
	}

	record Default(String replacement, String delimiter) {
		@Override
		public String toString() {
			return delimiter + replacement;
		}
	}
}
