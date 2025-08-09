package com.wiredi.runtime.properties.accessor;

import com.wiredi.runtime.lang.ThrowingConsumer;
import com.wiredi.runtime.lang.ThrowingFunction;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A powerful interface for handling potentially null values in a fluent, functional style.
 * <p>
 * The PropertyAccessor provides a safe way to work with values that might be null,
 * allowing operations like transformation, consumption, and fallback values to be
 * chained together without null checks or conditional logic.
 * <p>
 * Key features:
 * <ul>
 *   <li>Null-safe operations - no NullPointerExceptions</li>
 *   <li>Fluent API for method chaining</li>
 *   <li>Support for transformations via {@link #map(ThrowingFunction)}</li>
 *   <li>Support for side effects via {@link #applyTo(ThrowingConsumer)}</li>
 *   <li>Support for fallback values via {@link #or(Object)} and {@link #orElse(ThrowingSupplier)}</li>
 * </ul>
 * <p>
 * Common use cases:
 * <ul>
 *   <li>Safely accessing and transforming potentially null values</li>
 *   <li>Providing default values when a value is null</li>
 *   <li>Performing operations only when a value is present</li>
 *   <li>Building data transformation pipelines that handle nulls gracefully</li>
 *   <li>Implementing the Optional pattern with additional functionality</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * // Transform a value and provide a default if null
 * String result = PropertyAccessor.of(someValue)
 *     .map(String::toUpperCase)
 *     .or("default")
 *     .get();
 *
 * // Perform an operation only if value is not null
 * PropertyAccessor.of(someValue)
 *     .applyTo(value -> logger.info("Value: {}", value));
 *
 * // Chain multiple operations with fallbacks
 * int length = PropertyAccessor.of(someValue)
 *     .map(String::trim)
 *     .or("")
 *     .map(String::length)
 *     .get();
 * }</pre>
 *
 * @param <T> the type of value this accessor may contain
 * @see SimplePropertyAccessor
 * @see EmptyPropertyAccessor
 * @see PropertyContainer
 */
public interface PropertyAccessor<T> {

    /**
     * A shared instance of an empty property accessor.
     * <p>
     * This constant is used internally by the {@link #empty()} method to avoid
     * creating new instances of {@link EmptyPropertyAccessor} unnecessarily.
     */
    PropertyAccessor<?> EMPTY = new EmptyPropertyAccessor();

    /**
     * Creates an empty property accessor that contains no value.
     * <p>
     * This method returns a type-safe empty accessor that can be used in place
     * of null. Operations like {@link #map(ThrowingFunction)} and {@link #applyTo(ThrowingConsumer)}
     * will have no effect on an empty accessor, while {@link #or(Object)} and
     * {@link #orElse(ThrowingSupplier)} can be used to provide fallback values.
     * <p>
     * Example usage:
     * <pre>{@code
     * PropertyAccessor<String> empty = PropertyAccessor.empty();
     * String value = empty.or("default").get(); // Returns "default"
     * }</pre>
     *
     * @param <S> the type parameter for the returned empty accessor
     * @return an empty property accessor
     * @see EmptyPropertyAccessor
     */
    static <S> PropertyAccessor<S> empty() {
        return (PropertyAccessor<S>) EMPTY;
    }

    /**
     * Creates a property accessor for the given value, which may be null.
     * <p>
     * This factory method is the primary entry point for creating PropertyAccessor instances.
     * If the value is not null, it returns a {@link SimplePropertyAccessor} containing the value.
     * If the value is null, it returns an {@link EmptyPropertyAccessor}.
     * <p>
     * Example usage:
     * <pre>{@code
     * // With non-null value
     * PropertyAccessor<String> nonEmpty = PropertyAccessor.of("hello");
     * String value = nonEmpty.get(); // Returns "hello"
     *
     * // With null value
     * PropertyAccessor<String> empty = PropertyAccessor.of(null);
     * String value = empty.get(); // Returns null
     * String defaulted = empty.or("default").get(); // Returns "default"
     * }</pre>
     *
     * @param <S> the type of the value
     * @param value the value to wrap, may be null
     * @return a property accessor containing the value, or an empty accessor if the value is null
     * @see SimplePropertyAccessor
     * @see EmptyPropertyAccessor
     */
    static <S> PropertyAccessor<S> of(@Nullable S value) {
        if (value != null) {
            return new SimplePropertyAccessor<>(value);
        } else {
            return empty();
        }
    }

    /**
     * Transforms the value contained in this accessor using the provided mapping function.
     * <p>
     * If this accessor contains a value, the mapper function is applied to transform
     * the value, and a new accessor containing the result is returned. If the result
     * of the transformation is null, an empty accessor is returned.
     * <p>
     * If this accessor is empty, the mapper is not called, and an empty accessor is returned.
     * <p>
     * This method is useful for transforming values in a null-safe way and for building
     * transformation pipelines that can handle null values gracefully.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Transform a string to its length
     * PropertyAccessor<Integer> length = PropertyAccessor.of("hello")
     *     .map(String::length); // Returns accessor containing 5
     *
     * // Chain multiple transformations
     * PropertyAccessor<Integer> result = PropertyAccessor.of("  hello  ")
     *     .map(String::trim)
     *     .map(String::length); // Returns accessor containing 5
     *
     * // Handle null values
     * PropertyAccessor<Integer> empty = PropertyAccessor.of(null)
     *     .map(String::length); // Returns empty accessor
     * }</pre>
     *
     * @param <S> the type of the result of the transformation
     * @param <E> the type of exception that might be thrown by the mapper
     * @param mapper the function to apply to the value, if present
     * @return a new accessor containing the transformed value, or an empty accessor if this accessor is empty or the transformation result is null
     * @throws E if the mapper throws an exception
     */
    @NotNull
    <S, E extends Throwable> PropertyAccessor<S> map(@NotNull ThrowingFunction<T, S, E> mapper) throws E;

    /**
     * Applies the provided consumer to the value contained in this accessor, if present.
     * <p>
     * If this accessor contains a value, the consumer is called with the value.
     * If this accessor is empty, the consumer is not called.
     * <p>
     * This method is useful for performing side effects (like logging) only when a value is present,
     * without breaking the method chain.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Log a value if present
     * PropertyAccessor.of("hello")
     *     .applyTo(value -> logger.info("Value: {}", value))
     *     .map(String::length); // Logs "Value: hello" and returns accessor containing 5
     *
     * // No action for empty accessor
     * PropertyAccessor.of(null)
     *     .applyTo(value -> logger.info("Value: {}", value)); // Consumer not called
     * }</pre>
     *
     * @param <E> the type of exception that might be thrown by the consumer
     * @param consumer the consumer to apply to the value, if present
     * @return this accessor, for method chaining
     * @throws E if the consumer throws an exception
     */
    @NotNull
    <E extends Throwable> PropertyAccessor<T> applyTo(@NotNull ThrowingConsumer<T, E> consumer) throws E;

    /**
     * Provides a fallback value from the supplier if this accessor is empty.
     * <p>
     * If this accessor contains a value, the supplier is not called, and this accessor is returned.
     * If this accessor is empty, the supplier is called to provide a fallback value.
     * If the supplier returns a non-null value, a new accessor containing that value is returned.
     * If the supplier returns null, an empty accessor is returned.
     * <p>
     * This method is useful for providing dynamically computed fallback values when a value is absent.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Fallback not used for non-empty accessor
     * PropertyAccessor<String> nonEmpty = PropertyAccessor.of("hello")
     *     .orElse(() -> "default"); // Returns accessor containing "hello"
     *
     * // Fallback used for empty accessor
     * PropertyAccessor<String> withFallback = PropertyAccessor.of(null)
     *     .orElse(() -> "default"); // Returns accessor containing "default"
     *
     * // Expensive computation only performed if needed
     * PropertyAccessor<String> withExpensiveFallback = PropertyAccessor.of(null)
     *     .orElse(() -> computeExpensiveDefault()); // Only calls computeExpensiveDefault() if accessor is empty
     * }</pre>
     *
     * @param <S> the type of the fallback value, must be a subtype of T
     * @param <E> the type of exception that might be thrown by the supplier
     * @param supplier the supplier to provide a fallback value if this accessor is empty
     * @return this accessor if it contains a value, or a new accessor containing the fallback value, or an empty accessor if the supplier returns null
     * @throws E if the supplier throws an exception
     */
    @NotNull
    <S extends T, E extends Throwable> PropertyAccessor<T> orElse(@NotNull ThrowingSupplier<S, E> supplier) throws E;

    /**
     * Provides a fallback value if this accessor is empty.
     * <p>
     * If this accessor contains a value, it is returned unchanged.
     * If this accessor is empty and the fallback value is not null, a new accessor containing the fallback value is returned.
     * If this accessor is empty and the fallback value is null, an empty accessor is returned.
     * <p>
     * This method is useful for providing static fallback values when a value is absent.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Fallback not used for non-empty accessor
     * PropertyAccessor<String> nonEmpty = PropertyAccessor.of("hello")
     *     .or("default"); // Returns accessor containing "hello"
     *
     * // Fallback used for empty accessor
     * PropertyAccessor<String> withFallback = PropertyAccessor.of(null)
     *     .or("default"); // Returns accessor containing "default"
     *
     * // Chain multiple fallbacks
     * PropertyAccessor<String> withMultipleFallbacks = PropertyAccessor.of(null)
     *     .or(primaryFallback)
     *     .or(secondaryFallback)
     *     .or("last resort"); // Uses the first non-null fallback
     * }</pre>
     *
     * @param <S> the type of the fallback value, must be a subtype of T
     * @param value the fallback value to use if this accessor is empty
     * @return this accessor if it contains a value, or a new accessor containing the fallback value, or an empty accessor if the fallback value is null
     */
    @NotNull
    <S extends T> PropertyAccessor<T> or(@Nullable S value);

    /**
     * Retrieves the value contained in this accessor, which may be null.
     * <p>
     * This method is typically called at the end of a method chain to extract the final value.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Get a non-null value
     * String value = PropertyAccessor.of("hello").get(); // Returns "hello"
     *
     * // Get a null value
     * String nullValue = PropertyAccessor.of(null).get(); // Returns null
     *
     * // Get a value with fallback
     * String defaulted = PropertyAccessor.of(null).or("default").get(); // Returns "default"
     * }</pre>
     *
     * @return the value contained in this accessor, or null if this accessor is empty
     */
    @Nullable
    T get();
}
