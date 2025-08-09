package com.wiredi.runtime.properties.accessor;

import com.wiredi.runtime.lang.ThrowingConsumer;
import com.wiredi.runtime.lang.ThrowingFunction;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A concrete implementation of {@link PropertyAccessor} that contains a non-null value.
 * <p>
 * SimplePropertyAccessor is used when a non-null value is provided to {@link PropertyAccessor#of(Object)}.
 * It provides implementations of all PropertyAccessor methods that operate on the contained value.
 * <p>
 * Key behaviors:
 * <ul>
 *   <li>Always contains a non-null value</li>
 *   <li>{@link #map(ThrowingFunction)} applies the transformation to the value</li>
 *   <li>{@link #applyTo(ThrowingConsumer)} calls the consumer with the value</li>
 *   <li>{@link #orElse(ThrowingSupplier)} and {@link #or(Object)} ignore fallbacks since a value is already present</li>
 *   <li>{@link #get()} always returns a non-null value</li>
 * </ul>
 * <p>
 * This class is not typically instantiated directly by users. Instead, use the factory method
 * {@link PropertyAccessor#of(Object)} which will create a SimplePropertyAccessor when the value is non-null.
 * <p>
 * Example usage through PropertyAccessor:
 * <pre>{@code
 * // Create a SimplePropertyAccessor indirectly
 * PropertyAccessor<String> accessor = PropertyAccessor.of("hello");
 * 
 * // Transform the value
 * PropertyAccessor<Integer> lengthAccessor = accessor.map(String::length);
 * 
 * // Apply a consumer to the value
 * accessor.applyTo(value -> System.out.println("Value: " + value));
 * 
 * // Get the value
 * String value = accessor.get(); // Returns "hello"
 * }</pre>
 *
 * @param <T> the type of the non-null value contained in this accessor
 * @see PropertyAccessor
 * @see EmptyPropertyAccessor
 */
public class SimplePropertyAccessor<T> implements PropertyAccessor<T> {

    /**
     * The non-null value contained in this accessor.
     */
    @NotNull
    private final T value;

    /**
     * Constructs a new SimplePropertyAccessor containing the specified non-null value.
     * <p>
     * This constructor is typically not called directly by users. Instead, use
     * {@link PropertyAccessor#of(Object)} which will create a SimplePropertyAccessor
     * when the value is non-null.
     *
     * @param value the non-null value to be contained in this accessor
     * @throws NullPointerException if the value is null
     */
    public SimplePropertyAccessor(@NotNull T value) {
        this.value = value;
    }

    /**
     * Transforms the value contained in this accessor using the provided mapping function.
     * <p>
     * The mapper function is applied to the value, and a new accessor containing the result is returned.
     * If the result of the transformation is null, an empty accessor is returned.
     * <p>
     * This method allows for transforming the value while maintaining the fluent API style
     * and handling potential null results gracefully.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Transform a string to its length
     * PropertyAccessor<Integer> lengthAccessor = PropertyAccessor.of("hello")
     *     .map(String::length); // Returns SimplePropertyAccessor containing 5
     * 
     * // Transform to a null result
     * PropertyAccessor<String> emptyAccessor = PropertyAccessor.of("hello")
     *     .map(s -> null); // Returns EmptyPropertyAccessor
     * }</pre>
     *
     * @param <S> the type of the result of the transformation
     * @param <E> the type of exception that might be thrown by the mapper
     * @param mapper the function to apply to the value
     * @return a new accessor containing the transformed value, or an empty accessor if the transformation result is null
     * @throws E if the mapper throws an exception
     */
    @Override
    public <S, E extends Throwable> @NotNull PropertyAccessor<S> map(@NotNull ThrowingFunction<T, S, E> mapper) throws E {
        S result = mapper.apply(value);
        if (result != null) {
            return new SimplePropertyAccessor<>(result);
        }

        return PropertyAccessor.empty();
    }

    /**
     * Applies the provided consumer to the value contained in this accessor.
     * <p>
     * The consumer is called with the value, and this accessor is returned for method chaining.
     * This method is useful for performing side effects (like logging) without breaking the method chain.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Log a value and continue the chain
     * String result = PropertyAccessor.of("hello")
     *     .applyTo(value -> System.out.println("Value: " + value))
     *     .map(String::toUpperCase)
     *     .get(); // Prints "Value: hello" and returns "HELLO"
     * }</pre>
     *
     * @param <E> the type of exception that might be thrown by the consumer
     * @param consumer the consumer to apply to the value
     * @return this accessor, for method chaining
     * @throws E if the consumer throws an exception
     */
    @Override
    public <E extends Throwable> @NotNull SimplePropertyAccessor<T> applyTo(@NotNull ThrowingConsumer<T, E> consumer) throws E {
        consumer.accept(value);
        return this;
    }

    /**
     * Returns this accessor unchanged, ignoring the supplier.
     * <p>
     * Since this accessor already contains a non-null value, the supplier is not called,
     * and this accessor is returned as is. This behavior allows for providing fallback values
     * in a method chain without affecting accessors that already have values.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Supplier is not called because accessor already has a value
     * PropertyAccessor<String> accessor = PropertyAccessor.of("hello")
     *     .orElse(() -> {
     *         System.out.println("Computing fallback"); // This is not executed
     *         return "fallback";
     *     }); // Returns the original accessor with "hello"
     * }</pre>
     *
     * @param <S> the type of the fallback value, must be a subtype of T
     * @param <E> the type of exception that might be thrown by the supplier
     * @param supplier the supplier to provide a fallback value (not used in this implementation)
     * @return this accessor
     */
    @Override
    public @NotNull <S extends T, E extends Throwable> PropertyAccessor<T> orElse(@NotNull ThrowingSupplier<S, E> supplier) throws E {
        return this;
    }

    /**
     * Returns this accessor unchanged, ignoring the fallback value.
     * <p>
     * Since this accessor already contains a non-null value, the fallback value is ignored,
     * and this accessor is returned as is. This behavior allows for providing fallback values
     * in a method chain without affecting accessors that already have values.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Fallback is ignored because accessor already has a value
     * PropertyAccessor<String> accessor = PropertyAccessor.of("hello")
     *     .or("fallback"); // Returns the original accessor with "hello"
     * }</pre>
     *
     * @param <S> the type of the fallback value, must be a subtype of T
     * @param value the fallback value (not used in this implementation)
     * @return this accessor
     */
    @Override
    public @NotNull <S extends T> PropertyAccessor<T> or(@Nullable S value) {
        return this;
    }

    /**
     * Retrieves the non-null value contained in this accessor.
     * <p>
     * This method is typically called at the end of a method chain to extract the final value.
     * Since SimplePropertyAccessor always contains a non-null value, this method always returns a non-null value.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Get the value
     * String value = PropertyAccessor.of("hello").get(); // Returns "hello"
     * }</pre>
     *
     * @return the non-null value contained in this accessor
     */
    @Override
    public @NotNull T get() {
        return value;
    }
}
