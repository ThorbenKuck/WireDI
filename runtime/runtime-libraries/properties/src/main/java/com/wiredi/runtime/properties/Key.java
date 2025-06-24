package com.wiredi.runtime.properties;

import com.wiredi.runtime.properties.keys.CamelToKebabCaseKey;
import com.wiredi.runtime.properties.keys.PreFormattedKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Key is an in memory representation for a property key.
 * <p>
 * Implementations are required to overwrite equals and hash code. A key must be equal to another key, if the values
 * are the same.
 * <p>
 * Different implementations can handle keys differently. One build in implementation, the {@link CamelToKebabCaseKey}
 * will (for example) format the provided key to be conformed to the kebab standard. This helps with property file
 * containing non-kebab case keys (like <pre>my.customKey</pre>), whilst code references as kebab case
 * (like <pre>my.custom-key</pre>).
 * <p>
 * Though the problem would be, that it would be quite computationally expensive if every key would need to be converted.
 * This is why the annotation processors pre-format the keys and then construct a {@link PreFormattedKey} that is
 * already in kebab case.
 * <p>
 * To support but not enforce this standard and to eliminate computational complexity that can be prevented, the format
 * of the key is left open to the implementation of the Key and is not assumed by the properties.
 */
public interface Key {

    static String joinWithSeparator(
            @NotNull final String delimiter,
            @NotNull final String prefix,
            @NotNull final String suffix
    ) {
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

    /**
     * Constructs a preformatted key, not formatting the provided value
     *
     * @param value the pre-formatted key
     * @return a Key instance for the pre-formatted key
     */
    static PreFormattedKey just(@NotNull final String value) {
        return new PreFormattedKey(value);
    }

    /**
     * Constructs a new Key that formats the provided value to adhere to the standard.
     * <p>
     * Depending on the length of the key, this might be complex.
     *
     * @param value the key to format
     * @return a new instance of the pre-formatted key.
     */
    static CamelToKebabCaseKey format(@NotNull final String value) {
        return new CamelToKebabCaseKey(value);
    }

    @NotNull
    String value();

    @NotNull
    Key withPrefix(@NotNull final String prefix);

    @NotNull
    Key withSuffix(@NotNull final String suffix);
}
