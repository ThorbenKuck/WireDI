package com.wiredi.runtime.values;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This value holds a content and can never be empty.
 * <p>
 * Any null content provided to this value will result in a NullPointerException.
 *
 * @param <T>
 */
public class NeverNullValue<T> implements Value<T> {

    @NotNull
    private T content;

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
