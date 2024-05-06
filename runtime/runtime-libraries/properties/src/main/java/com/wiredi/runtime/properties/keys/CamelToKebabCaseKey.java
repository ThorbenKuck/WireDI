package com.wiredi.runtime.properties.keys;

import com.wiredi.runtime.properties.Key;
import com.wiredi.runtime.values.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.wiredi.runtime.properties.Key.joinWithSeparator;

public final class CamelToKebabCaseKey implements Key {

    @NotNull
    private final String value;

    @NotNull
    private final Value<String> formatted;

    public CamelToKebabCaseKey(@NotNull final String value) {
        this.value = value;
        this.formatted = Value.lazy(() -> camelToKebabCase(value));
    }

    @NotNull
    private static String camelToKebabCase(@NotNull final String name) {
        StringBuilder result = new StringBuilder();
        char c = name.charAt(0);
        result.append(c);
        for (int i = 1; i < name.length(); i++) {
            char previous = name.charAt(i - 1);
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch) && Character.isLowerCase(previous)) {
                result.append('-');
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    @Override
    @NotNull
    public String value() {
        return formatted.get();
    }

    @Override
    public @NotNull Key withPrefix(@NotNull String prefix) {
        return new CamelToKebabCaseKey(joinWithSeparator(".", prefix, value));
    }

    @Override
    public @NotNull Key withSuffix(@NotNull String suffix) {
        return new CamelToKebabCaseKey(joinWithSeparator(".", value, suffix));
    }

    @Override
    public boolean equals(@Nullable final Object o) {
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
    @NotNull
    public String toString() {
        return value();
    }

    public Key compile() {
        formatted.get();
        return this;
    }
}
