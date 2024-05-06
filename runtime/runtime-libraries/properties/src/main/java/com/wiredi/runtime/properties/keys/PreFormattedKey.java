package com.wiredi.runtime.properties.keys;

import com.wiredi.runtime.properties.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.wiredi.runtime.properties.Key.joinWithSeparator;

/**
 * A pre-formatted key is a key that will be interpreted "as is".
 *
 * @param value The key which should be represented
 */
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
		return new PreFormattedKey(joinWithSeparator(".", prefix, value));
	}

	@Override
	public @NotNull Key withSuffix(@NotNull String suffix) {
		return new PreFormattedKey(joinWithSeparator(".", value, suffix));
	}
}
