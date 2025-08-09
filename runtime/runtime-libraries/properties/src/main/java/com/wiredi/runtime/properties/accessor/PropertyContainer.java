package com.wiredi.runtime.properties.accessor;

import org.jetbrains.annotations.Nullable;

/**
 * An abstract base class that provides a convenient way to work with potentially null properties.
 * <p>
 * PropertyContainer serves as a foundation for classes that need to handle properties that might be null.
 * It provides a protected {@link #property(Object)} method that creates a {@link PropertyAccessor} for a value,
 * allowing for fluent, null-safe operations on that value.
 * <p>
 * Key features:
 * <ul>
 *   <li>Simplifies working with potentially null properties</li>
 *   <li>Enables fluent, method-chaining API for property access and transformation</li>
 *   <li>Provides a consistent pattern for handling default values</li>
 *   <li>Makes code more readable by eliminating null checks and conditional logic</li>
 * </ul>
 * <p>
 * Common use cases:
 * <ul>
 *   <li>Data transfer objects (DTOs) with optional fields</li>
 *   <li>Configuration objects that need to provide default values</li>
 *   <li>Domain objects that need to transform properties safely</li>
 *   <li>Any class that needs to handle potentially null properties in a clean, functional way</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * public class UserSettings extends PropertyContainer {
 *     private final String username;
 *     private final Integer maxItems;
 *     private final Boolean notifications;
 *
 *     public UserSettings(String username, Integer maxItems, Boolean notifications) {
 *         this.username = username;
 *         this.maxItems = maxItems;
 *         this.notifications = notifications;
 *     }
 *
 *     public String getUsername() {
 *         return property(username)
 *                 .or("guest")
 *                 .get();
 *     }
 *
 *     public int getMaxItems() {
 *         return property(maxItems)
 *                 .or(10)
 *                 .get();
 *     }
 *
 *     public boolean isNotificationsEnabled() {
 *         return property(notifications)
 *                 .or(false)
 *                 .get();
 *     }
 *
 *     public Map<String, Object> toMap() {
 *         Map<String, Object> result = new HashMap<>();
 *
 *         property(username)
 *                 .applyTo(value -> result.put("username", value));
 *         property(maxItems)
 *                 .applyTo(value -> result.put("maxItems", value));
 *         property(notifications)
 *                 .applyTo(value -> result.put("notifications", value));
 *
 *         return result;
 *     }
 * }
 * }</pre>
 *
 * @see PropertyAccessor
 * @see SimplePropertyAccessor
 * @see EmptyPropertyAccessor
 */
public abstract class PropertyContainer {

    /**
     * Creates a PropertyAccessor for the given value, which may be null.
     * <p>
     * This method is the core utility provided by PropertyContainer. It wraps a potentially
     * null value in a PropertyAccessor, allowing for fluent, null-safe operations on that value.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Provide a default value for a potentially null property
     * String name = property(username)
     *     .or("guest")
     *     .get();
     *
     * // Transform a property only if it's not null
     * Integer length = property(text)
     *     .map(String::length)
     *     .get();
     *
     * // Perform an operation only if a property is not null
     * property(email)
     *     .applyTo(value -> sendEmail(value));
     *
     * // Chain multiple operations with fallbacks
     * String processed = property(text)
     *     .map(String::trim)
     *     .or("")
     *     .map(String::toUpperCase)
     *     .get();
     * }</pre>
     *
     * @param <T> the type of the value
     * @param value the value to wrap, may be null
     * @return a property accessor containing the value, or an empty accessor if the value is null
     * @see PropertyAccessor#of(Object)
     */
    protected <T> PropertyAccessor<T> property(@Nullable T value) {
        return PropertyAccessor.of(value);
    }
}
