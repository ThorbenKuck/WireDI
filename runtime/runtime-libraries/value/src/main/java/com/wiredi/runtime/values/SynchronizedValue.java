package com.wiredi.runtime.values;

import com.wiredi.runtime.async.DataAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.wiredi.runtime.lang.Preconditions.isNotNull;

/**
 * A synchronized value that is synchronizing read/write operations on the contained value.
 * <p>
 * This value will synchronize access to the contained value using a {@link DataAccess}.
 * Only one thread at a time can read/write the value at a time.
 * <p>
 * The value may be held simultaneously by multiple reader threads, so long as there are no writers.
 * Write access to this value is exclusive.
 *
 * @param <T>
 * @see DataAccess
 * @see Value
 */
public class SynchronizedValue<T> implements Value<T> {

    private final DataAccess dataAccess;
    @Nullable
    private T content;

    /**
     * Creates a new SynchronizedValue with the specified initial content and DataAccess.
     *
     * @param content    The initial content for this value, may be null
     * @param dataAccess The DataAccess to use for synchronization
     */
    public SynchronizedValue(@Nullable T content, DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        this.content = content;
    }

    /**
     * Creates a new SynchronizedValue with the specified initial content.
     * <p>
     * This constructor creates a new DataAccess for synchronization.
     *
     * @param content The initial content for this value, may be null
     */
    public SynchronizedValue(@Nullable T content) {
        this(content, new DataAccess());
    }

    /**
     * Creates a new empty SynchronizedValue.
     * <p>
     * The value will be initialized with null content, meaning {@link #isSet()}
     * will return false until a value is set using {@link #set(Object)}.
     * This constructor creates a new DataAccess for synchronization.
     */
    public SynchronizedValue() {
        this(null);
    }

    /**
     * Reads the current value and applies a mapping function to it in a thread-safe manner.
     * <p>
     * This method acquires a read lock before accessing the value, allowing multiple
     * threads to read the value concurrently but preventing writes during the read operation.
     *
     * @param mapper The function to apply to the current value
     * @param <S>    The type of the result after applying the mapping function
     * @return The result of applying the mapping function to the current value
     */
    public <S> S read(Function<T, S> mapper) {
        return dataAccess.readNullableValue(() -> mapper.apply(content));
    }

    /**
     * Updates the current value in a thread-safe manner.
     * <p>
     * This method acquires a write lock before updating the value, ensuring that
     * no other threads can read or write the value during the update operation.
     *
     * @param supplier The supplier that provides the new value
     */
    public void write(Supplier<@Nullable T> supplier) {
        dataAccess.write(() -> this.content = supplier.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull T get() {
        return read(current -> isNotNull(current, () -> "The value contained null, this is not allowed"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(@Nullable T t) {
        write(() -> t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSet() {
        return read(Objects::nonNull);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ifEmpty(@NotNull Runnable runnable) {
        T current = read(it -> it);

        if (current == null) {
            runnable.run();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull T getOrSet(@NotNull Supplier<@NotNull T> supplier) {
        return dataAccess.readValue(() -> Objects.requireNonNullElseGet(this.content, () -> dataAccess.writeValue(() -> {
            T newValue = supplier.get();
            this.content = newValue;
            return newValue;
        })));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull IfPresentStage ifPresent(@NotNull Consumer<@NotNull T> presentConsumer) {
        T current = read(it -> it);

        if (current != null) {
            presentConsumer.accept(current);
            return IfPresentStage.wasPresent();
        } else {
            return IfPresentStage.wasMissing();
        }
    }

    @Override
    public String toString() {
        return "SynchronizedValue{" +
                "content=" + content +
                '}';
    }
}
