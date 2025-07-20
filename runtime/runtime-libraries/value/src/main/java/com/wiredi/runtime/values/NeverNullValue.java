package com.wiredi.runtime.values;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A Value implementation that guarantees to never hold a null value.
 * <p>
 * This implementation ensures that the contained value is always non-null.
 * Any attempt to set a null value through {@link #set(Object)} will result in a NullPointerException.
 * As a result, {@link #isSet()} always returns true, and {@link #ifEmpty(Runnable)} never executes the runnable.
 * <p>
 * This class is useful when you need to guarantee that a value is always available and never null,
 * eliminating the need for null checks when using the value.
 *
 * @param <T> The type of value stored in this container
 * @see Value#neverNull(Object)
 */
public class NeverNullValue<T> implements Value<T> {

    @NotNull
    private T content;

    /**
     * Creates a new NeverNullValue with the specified initial content.
     * <p>
     * The provided content must not be null, or a NullPointerException will be thrown.
     *
     * @param content The initial content for this value, must not be null
     * @throws NullPointerException if the content is null
     */
    public NeverNullValue(@NotNull T content) {
        this.content = Objects.requireNonNull(content);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(@Nullable T t) {
        this.content = Objects.requireNonNull(t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSet() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ifEmpty(@NotNull Runnable runnable) {
        // This cannot happen, has this value always has to be set
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull T getOrSet(Supplier<@NotNull T> supplier) {
        return this.content;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull IfPresentStage ifPresent(@NotNull Consumer<@NotNull T> presentConsumer) {
        presentConsumer.accept(content);
        return IfPresentStage.wasPresent();
    }

    /**
     * Attempts to set the value, but only if the provided value is not null.
     * <p>
     * Unlike {@link #set(Object)}, this method does not throw a NullPointerException
     * if the provided value is null. Instead, it silently ignores null values,
     * keeping the current content unchanged.
     *
     * @param t The value to set, may be null
     */
    public void trySet(@Nullable T t) {
        if (t != null) {
            this.content = t;
        }
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public T get() {
        return this.content;
    }

    @Override
    public String toString() {
        return "NeverNullValue{" +
                "content=" + content +
                '}';
    }
}
