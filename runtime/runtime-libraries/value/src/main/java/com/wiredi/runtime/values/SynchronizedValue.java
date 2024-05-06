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

    public SynchronizedValue(@Nullable T content, DataAccess dataAccess) {
        this.dataAccess = dataAccess;
        this.content = content;
    }

    public SynchronizedValue(@Nullable T content) {
        this(content, new DataAccess());
    }

    public SynchronizedValue() {
        this(null);
    }

    public <S> S read(Function<T, S> mapper) {
        return dataAccess.readNullableValue(() -> mapper.apply(content));
    }

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
