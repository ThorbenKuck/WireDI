package com.wiredi.runtime.values;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.wiredi.runtime.lang.Preconditions.isNotNull;

/**
 * This value is the base implementation of a Value.
 *
 * @param <T>
 * @see Value
 */
public class SimpleValue<T> implements Value<T> {

    @Nullable
    private T content;

    public SimpleValue(@Nullable T content) {
        this.content = content;
    }

    public SimpleValue() {
        this.content = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull T get() {
        return isNotNull(content, () -> "Value was empty");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(@Nullable T t) {
        this.content = t;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSet() {
        return content != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ifEmpty(@NotNull Runnable runnable) {
        T current = content;

        if (current == null) {
            runnable.run();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull T getOrSet(Supplier<@NotNull T> supplier) {
        if (this.content == null) {
            T newContent = supplier.get();
            this.content = newContent;
            return newContent;
        } else {
            return this.content;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull IfPresentStage ifPresent(@NotNull Consumer<T> presentConsumer) {
        T current = content;

        if (current != null) {
            presentConsumer.accept(current);
            return IfPresentStage.wasPresent();
        } else {
            return IfPresentStage.wasMissing();
        }
    }

    @Override
    public String toString() {
        return "SimpleValue{" +
                "content=" + content +
                '}';
    }
}
