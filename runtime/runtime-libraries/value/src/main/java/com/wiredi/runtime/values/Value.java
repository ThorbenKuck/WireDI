package com.wiredi.runtime.values;

import com.wiredi.runtime.async.AsyncLoader;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A Value is a representation of some value.
 * <p>
 * Details of how the value is created and maintained is dependent on the implementation.
 * <p>
 * Any value is supporting null values, though a Value is never allowed to return null.
 * If a value contains null and {@link #get()} is called, implementations are required to throw a
 * {@link NullPointerException} and not return null.
 *
 * @param <T> the generic of the type.
 */
public interface Value<T> {

    /**
     * Constructs a value that eagerly and asynchronously fetches a value from the provided Supplier.
     * <p>
     * The concrete implementation will use the {@link AsyncLoader} to asynchronously dispatch the Supplier.
     * If the value state is request before the separate Thread/Fiber/Runnable is done fetching the value,
     * the implementation will block the get call until the parallel process is finished.
     *
     * @param supplier the supplier to fetch the data
     * @param <T>      the generic type
     * @return a value that will block until the async process is finished
     * @see FutureValue
     */
    @NotNull
    static <T> Value<@NotNull T> async(@NotNull ThrowingSupplier<@NotNull T, ?> supplier) {
        return FutureValue.of(supplier);
    }

    /**
     * Constructs a value that is evaluated on demand.
     * <p>
     * Only on the first get call will the supplier be asked to construct the instance. After that it will
     * be cached and returned.
     *
     * @param supplier the supplier to construct the instance when asked.
     * @param <T>      the type of the value
     * @return the value that lazily initializes the value
     * @see LazyValue
     */
    @NotNull
    static <T, E extends Throwable> Value<@NotNull T> lazy(@NotNull ThrowingSupplier<@Nullable T, E> supplier) {
        return new LazyValue<>(supplier);
    }

    /**
     * Constructs a new value of a supplier.
     * <p>
     * The resulting Value is stateless and just delegated to the supplier.
     *
     * @param supplier the supplier to invoke
     * @param <T>      the type produced by the supplier
     * @param <E>      the exception type that could be raised
     * @return a new, stateless value
     */
    static <T, E extends Throwable> Value<@NotNull T> of(@NotNull ThrowingSupplier<@Nullable T, E> supplier) {
        return new LazyStatelessValue<>(supplier);
    }

    /**
     * Constructs a value that can never hold a null value.
     * <p>
     * A value constructed like this cannot be filled with null.
     * Calling {@link #set(Object)} with null on this value will throw a {@link NullPointerException}
     *
     * @param t   the parameter to hold, never null
     * @param <T> the type of the value
     * @return a Value that can never be filled with null
     * @see NeverNullValue
     */
    @NotNull
    static <T> Value<@NotNull T> neverNull(@NotNull T t) {
        return new NeverNullValue<>(t);
    }

    /**
     * Constructs a thread safe value.
     * <p>
     * This value utilizes ReadWriteLocks to synchronize over the provided value.
     * Calling {@link #set(Object)} will acquire a write lock, whilst {@link #get()}, {@link #ifEmpty(Runnable)} and
     * {@link #ifPresent(Consumer)} will acquire a read lock.
     *
     * @param t   the initial value to synchronize over
     * @param <T> the type of the value
     * @return a synchronized, thread safe value
     * @see SynchronizedValue
     * @see java.util.concurrent.locks.ReentrantReadWriteLock
     */
    @NotNull
    static <T> Value<@NotNull T> synchronize(@NotNull T t) {
        return new SynchronizedValue<>(t);
    }

    /**
     * Constructs an empty thread safe value.
     * <p>
     * This value utilizes ReadWriteLocks to synchronize over the provided value.
     * Calling {@link #set(Object)} will acquire a write lock, whilst {@link #get()}, {@link #ifEmpty(Runnable)} and
     * {@link #ifPresent(Consumer)} will acquire a read lock.
     *
     * @param <T> the type of the value
     * @return a synchronized, thread safe value
     * @see SynchronizedValue
     * @see java.util.concurrent.locks.ReentrantReadWriteLock
     */
    @NotNull
    static <T> Value<@NotNull T> synchronize() {
        return new SynchronizedValue<>();
    }

    /**
     * Constructs a simple value for a type.
     *
     * @param t   the initial value to simple value
     * @param <T> the type of the value
     * @return a synchronized, thread safe value
     * @see SimpleValue
     */
    @NotNull
    static <T> Value<@NotNull T> just(@Nullable T t) {
        return new SimpleValue<>(t);
    }

    /**
     * Constructs an empty simple value for a type.
     *
     * @param <T> the type of the value
     * @return a synchronized, thread safe value
     * @see SimpleValue
     */
    @NotNull
    static <T> Value<@NotNull T> empty() {
        return new SimpleValue<>();
    }

    /**
     * Returns the content of the value.
     * <p>
     * As null values are not allowed, this method must throw a {@link NullPointerException}, if the content is null.
     *
     * @return the content of the value
     * @throws NullPointerException if the value is null
     */
    @NotNull
    T get();

    /**
     * Returns the content of the value, or the result of the defaultValue supplier if the value is not set.
     * <p>
     * This method provides a way to get a value with a fallback that can throw exceptions.
     * If the value is not set, the defaultValue supplier will be called to provide a fallback value.
     *
     * @param defaultValue The supplier to provide a fallback value if this value is not set
     * @param <E>          The type of exception that the supplier might throw
     * @return The content of this value if set, otherwise the result of the defaultValue supplier
     * @throws E If the defaultValue supplier throws an exception
     */
    default <E extends Throwable> T get(ThrowingSupplier<T, E> defaultValue) throws E {
        if (!isSet()) {
            return defaultValue.get();
        } else {
            return get();
        }
    }

    /**
     * Returns the content of the value, or the defaultValue if the value is not set.
     * <p>
     * This method provides a way to get a value with a simple fallback.
     * If the value is not set, the defaultValue will be returned instead.
     *
     * @param defaultValue The fallback value to return if this value is not set
     * @return The content of this value if set, otherwise the defaultValue
     */
    default T get(T defaultValue) {
        if (!isSet()) {
            return defaultValue;
        } else {
            return get();
        }
    }

    /**
     * Set the content of the value.
     * <p>
     * Might be null, though implementations may restrict this behaviour.
     *
     * @param t the value to use
     */
    void set(@Nullable T t);

    /**
     * Check if the value is set.
     * <p>
     * The value is considered set if it contains a non-null value.
     * This method is the opposite of {@link #isEmpty()}.
     *
     * @return true if the value is set, false if it is empty.
     * @see #isEmpty()
     */
    boolean isSet();

    /**
     * Check if the value is empty.
     * <p>
     * Negates the {@link #isSet()} check.
     *
     * @return true if the value is empty, false if it is set.
     * @see #isSet()
     * @see #ifEmpty(Runnable)
     */
    default boolean isEmpty() {
        return !isSet();
    }

    /**
     * The provided {@link Runnable} will be invoked if this value is empty.
     *
     * @param runnable the runnable to execute
     * @see #isEmpty()
     * @see #ifPresent(Consumer)
     */
    void ifEmpty(@NotNull Runnable runnable);

    /**
     * Will return the content of this value.
     * If it is empty, it will first set the content to the return value of the {@link Supplier}.
     * <p>
     * The provided {@link Supplier} is not allowed to return null values.
     * If the Supplier returned null, the value would be empty again and unable to return a Value.
     *
     * @param supplier the supplier that will set the value if it is absent.
     * @return the content of this value
     */
    @NotNull
    T getOrSet(Supplier<@NotNull T> supplier);

    /**
     * The provided {@link Consumer} will be invoked if this value contains a value.
     *
     * @param presentConsumer The consumer to execute
     * @return a Fluent instance to handle empty states
     * @see #isSet()
     * @see #ifEmpty(Runnable)
     * @see IfPresentStage
     */
    @NotNull
    IfPresentStage ifPresent(@NotNull Consumer<@NotNull T> presentConsumer);

    /**
     * Converts this value into a supplier instance.
     *
     * @return a new Supplier instance, that is delegating to the {@link #get()} method
     * @see #get()
     */
    @NotNull
    default Supplier<@Nullable T> asSupplier() {
        return this::get;
    }

    /**
     * Maps the content of this value using the provided mapping function.
     * <p>
     * This method retrieves the value using {@link #get()} and applies the mapping function to it.
     * It's a convenient way to transform the value without having to manually call get() and apply the function.
     *
     * @param mappingFunction The function to apply to the value
     * @param <S>             The type of the result after applying the mapping function
     * @return The result of applying the mapping function to the value
     * @throws NullPointerException If the value is not set and {@link #get()} throws
     */
    default <S> S map(Function<T, S> mappingFunction) {
        return mappingFunction.apply(get());
    }
}
