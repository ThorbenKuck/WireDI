package com.wiredi.properties.keys;

import com.wiredi.lang.SafeReference;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CamelToKebabCaseKey implements Key {

    private final SafeReference<String> formatted = SafeReference.empty();
    private final String value;

    public CamelToKebabCaseKey(String value) {
        this.value = value;
    }

    private static String camelToKebabCase(String name) {
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
        return formatted.getOrSet(() -> camelToKebabCase(value));
    }

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
}
