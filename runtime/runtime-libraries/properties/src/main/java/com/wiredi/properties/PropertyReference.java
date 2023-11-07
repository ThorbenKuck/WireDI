package com.wiredi.properties;

import com.wiredi.properties.exceptions.PropertyNotFoundException;
import com.wiredi.properties.keys.Key;
import org.jetbrains.annotations.NotNull;

/**
 * A reference for a concrete property.
 * <p>
 * You can use this reference if you want to constantly reference a property. For example like this:
 *
 * <pre><code>
 * public class MyClass {
 *     private final PropertyReference propertyReference;
 *
 *     public MyClass(TypedProperties typedProperties) {
 *         this.propertyReference = typedProperties.getPropertyReference(Key.just("my.property"));
 *     }
 * }
 * </code></pre>
 * <p>
 * This reference will resolve lazily and multiton, meaning that upon calling
 * {@link #getValue()}, {@link #getValue(String)}, {@link #getValue(Class)} or {@link #getValue(Class, Object)} the
 * value will be fetched from the connected {@link TypedProperties}. The value is not cached.
 */
public record PropertyReference(
        @NotNull TypedProperties typedProperties,
        @NotNull Key key
) {
    public String getValue() {
        return typedProperties.require(key);
    }

    @NotNull
    public String getValue(@NotNull final String defaultValue) {
        return typedProperties.get(key, defaultValue);
    }

    @NotNull
    public <T> T getValue(@NotNull final Class<T> type) {
        return typedProperties.getTyped(key, type).orElseThrow(() -> new PropertyNotFoundException(key));
    }

    @NotNull
    public <T> T getValue(
            @NotNull final Class<T> type,
            @NotNull final T defaultValue
    ) {
        return typedProperties.getTyped(key, type).orElse(defaultValue);
    }
}
