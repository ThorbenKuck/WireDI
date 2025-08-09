package com.wiredi.runtime.properties.accessor;

import com.wiredi.runtime.lang.ThrowingConsumer;
import com.wiredi.runtime.lang.ThrowingFunction;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A concrete implementation of {@link PropertyAccessor} that represents an empty/null value.
 * <p>
 * EmptyPropertyAccessor is used when a null value is provided to {@link PropertyAccessor#of(Object)}
 * or when {@link PropertyAccessor#empty()} is called. It provides implementations of all PropertyAccessor
 * methods that handle the absence of a value.
 * <p>
 * Key behaviors:
 * <ul>
 *   <li>Contains no value (represents null)</li>
 *   <li>{@link #map(ThrowingFunction)} returns an empty accessor without calling the mapper</li>
 *   <li>{@link #applyTo(ThrowingConsumer)} does nothing and returns this accessor</li>
 *   <li>{@link #orElse(ThrowingSupplier)} and {@link #or(Object)} provide fallback values</li>
 *   <li>{@link #get()} always returns null</li>
 * </ul>
 * <p>
 * This class is not typically instantiated directly by users. Instead, use the factory methods
 * {@link PropertyAccessor#of(Object)} with a null value or {@link PropertyAccessor#empty()}.
 * <p>
 * Example usage through PropertyAccessor:
 * <pre>{@code
 * // Create an EmptyPropertyAccessor indirectly
 * PropertyAccessor<String> accessor = PropertyAccessor.of(null);
 * // or
 * PropertyAccessor<String> accessor = PropertyAccessor.empty();
 * 
 * // Mapping has no effect
 * PropertyAccessor<Integer> lengthAccessor = accessor.map(String::length); // Still empty
 * 
 * // Consumer is not called
 * accessor.applyTo(value -> System.out.println("Value: " + value)); // Nothing happens
 * 
 * // Provide a fallback value
 * String value = accessor.or("default").get(); // Returns "default"
 * 
 * // Provide a dynamically computed fallback
 * String computed = accessor.orElse(() -> computeExpensiveDefault()).get();
 * }</pre>
 *
 * @see PropertyAccessor
 * @see SimplePropertyAccessor
 */
public class EmptyPropertyAccessor implements PropertyAccessor<Object> {
    
    /**
     * Returns an empty accessor without calling the mapper.
     * <p>
     * Since this accessor is empty (contains no value), the mapper function is not called,
     * and an empty accessor is returned. This behavior allows for building transformation
     * chains that gracefully handle null values.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Mapper is not called because accessor is empty
     * PropertyAccessor<Integer> lengthAccessor = PropertyAccessor.empty()
     *     .map(obj -> {
     *         System.out.println("Computing length"); // This is not executed
     *         return obj.toString().length();
     *     }); // Returns an empty accessor
     * 
     * // Chained maps have no effect
     * PropertyAccessor<String> result = PropertyAccessor.empty()
     *     .map(Object::toString)
     *     .map(String::toUpperCase); // Still empty
     * }</pre>
     *
     * @param <S> the type of the result of the transformation (not used in this implementation)
     * @param <E> the type of exception that might be thrown by the mapper (not used in this implementation)
     * @param mapper the function to apply to the value (not used in this implementation)
     * @return an empty accessor
     */
    @Override
    public <S, E extends Throwable> @NotNull PropertyAccessor<S> map(@NotNull ThrowingFunction<Object, S, E> mapper) throws E {
        return (PropertyAccessor<S>) this;
    }

    /**
     * Returns this accessor without calling the consumer.
     * <p>
     * Since this accessor is empty (contains no value), the consumer is not called,
     * and this accessor is returned. This behavior allows for performing side effects
     * only when a value is present, without breaking the method chain.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Consumer is not called because accessor is empty
     * PropertyAccessor<Object> accessor = PropertyAccessor.empty()
     *     .applyTo(value -> {
     *         System.out.println("Value: " + value); // This is not executed
     *     }); // Returns the same empty accessor
     * }</pre>
     *
     * @param <E> the type of exception that might be thrown by the consumer (not used in this implementation)
     * @param consumer the consumer to apply to the value (not used in this implementation)
     * @return this accessor
     */
    @Override
    public <E extends Throwable> @NotNull PropertyAccessor<Object> applyTo(@NotNull ThrowingConsumer<Object, E> consumer) throws E {
        return this;
    }

    /**
     * Provides a fallback value from the supplier if it returns a non-null value.
     * <p>
     * Since this accessor is empty (contains no value), the supplier is called to provide a fallback value.
     * If the supplier returns a non-null value, a new accessor containing that value is returned.
     * If the supplier returns null, this empty accessor is returned.
     * <p>
     * This method is useful for providing dynamically computed fallback values when a value is absent.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Supplier is called to provide a fallback
     * PropertyAccessor<String> accessor = PropertyAccessor.empty()
     *     .orElse(() -> {
     *         System.out.println("Computing fallback"); // This is executed
     *         return "fallback";
     *     }); // Returns a SimplePropertyAccessor containing "fallback"
     * 
     * // Supplier returning null
     * PropertyAccessor<String> stillEmpty = PropertyAccessor.empty()
     *     .orElse(() -> null); // Returns the same empty accessor
     * }</pre>
     *
     * @param <S> the type of the fallback value
     * @param <E> the type of exception that might be thrown by the supplier
     * @param supplier the supplier to provide a fallback value
     * @return a new accessor containing the fallback value, or this empty accessor if the supplier returns null
     * @throws E if the supplier throws an exception
     */
    @Override
    public @NotNull <S, E extends Throwable> PropertyAccessor<Object> orElse(@NotNull ThrowingSupplier<S, E> supplier) throws E {
        S value = supplier.get();
        if (value != null) {
            return new SimplePropertyAccessor<>(value);
        }
        return this;
    }

    /**
     * Provides a fallback value if it is not null.
     * <p>
     * Since this accessor is empty (contains no value), the fallback value is used if it is not null.
     * If the fallback value is not null, a new accessor containing that value is returned.
     * If the fallback value is null, this empty accessor is returned.
     * <p>
     * This method is useful for providing static fallback values when a value is absent.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Fallback is used
     * PropertyAccessor<String> accessor = PropertyAccessor.empty()
     *     .or("fallback"); // Returns a SimplePropertyAccessor containing "fallback"
     * 
     * // Null fallback has no effect
     * PropertyAccessor<String> stillEmpty = PropertyAccessor.empty()
     *     .or(null); // Returns the same empty accessor
     * 
     * // Chain multiple fallbacks
     * PropertyAccessor<String> withFallback = PropertyAccessor.empty()
     *     .or(primaryFallback) // If primaryFallback is null, continues to next fallback
     *     .or(secondaryFallback) // If secondaryFallback is null, continues to next fallback
     *     .or("last resort"); // Uses the first non-null fallback
     * }</pre>
     *
     * @param <S> the type of the fallback value
     * @param value the fallback value to use
     * @return a new accessor containing the fallback value, or this empty accessor if the fallback value is null
     */
    @Override
    public @NotNull <S> PropertyAccessor<Object> or(@Nullable S value) {
        if (value != null) {
            return new SimplePropertyAccessor<>(value);
        }

        return this;
    }

    /**
     * Returns null, indicating that this accessor is empty.
     * <p>
     * Since this accessor is empty (contains no value), this method always returns null.
     * <p>
     * Example usage:
     * <pre>{@code
     * // Get null from an empty accessor
     * Object value = PropertyAccessor.empty().get(); // Returns null
     * 
     * // Get a fallback value
     * String defaulted = PropertyAccessor.<String>empty()
     *     .or("default")
     *     .get(); // Returns "default"
     * }</pre>
     *
     * @return null
     */
    @Override
    public @Nullable Object get() {
        return null;
    }
}
