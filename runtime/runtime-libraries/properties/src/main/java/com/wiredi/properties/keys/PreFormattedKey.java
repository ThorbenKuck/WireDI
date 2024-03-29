package com.wiredi.properties.keys;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record PreFormattedKey(@NotNull String value) implements Key {
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (!(o instanceof Key that)) return false;

		return Objects.equals(value(), that.value());
	}

	@Override
	public int hashCode() {
		return Objects.hash(value());
	}

	@Override
	public String toString() {
		return value();
	}

	@Override
	public @NotNull Key withPrefix(@NotNull String prefix) {
		return new PreFormattedKey(Key.joinWithSeparator(".", prefix, value));
	}

	@Override
	public @NotNull Key withSuffix(@NotNull String suffix) {
		return new PreFormattedKey(Key.joinWithSeparator(".", value, suffix));
	}
}
