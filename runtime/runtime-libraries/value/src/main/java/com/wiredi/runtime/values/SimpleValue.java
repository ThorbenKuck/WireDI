package com.wiredi.runtime.values;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.wiredi.runtime.lang.Preconditions.isNotNull;

/**
 * A simple implementation of the {@link Value} interface.
 * <p>
 * This class provides a straightforward implementation of the Value interface
 * with basic storage and retrieval capabilities. It stores a single value that
 * can be null, and provides methods to access, modify, and check the state of that value.
 * <p>
 * SimpleValue is not thread-safe. For thread-safe operations, use {@link SynchronizedValue}.
 *
 * @param <T> The type of value stored in this container
 * @see Value
 * @see Value#just(Object)
 * @see Value#empty()
 */
public class SimpleValue<T> implements Value<T> {

    @Nullable
    private T content;

    /**
     * Creates a new SimpleValue with the specified initial content.
     *
     * @param content The initial content for this value, may be null
     */
    public SimpleValue(@Nullable T content) {
        this.content = content;
    }

    /**
     * Creates a new empty SimpleValue.
     * <p>
     * The value will be initialized with null content, meaning {@link #isSet()}
     * will return false until a value is set using {@link #set(Object)}.
     */
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
