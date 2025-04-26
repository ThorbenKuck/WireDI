package com.wiredi.runtime.values;

import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This value holds a content that is lazily resolved.
 *
 * Contrary to the  {@link LazyValue}, this Value doesn't hold a state.
 * Instead, it always consults the underlying supplier to retrieve its value.
 *
 * @param <T>
 */
public class LazyStatelessValue<T> implements Value<T> {

    @Nullable
    private ThrowingSupplier<T, ?> supplier;

    public LazyStatelessValue(@NotNull ThrowingSupplier<@Nullable T, ?> supplier) {
        this.supplier = supplier;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    private T getCurrent() {
        try {
            return supplier.get();
        } catch (Throwable e) {
            if (e instanceof RuntimeException r) {
                throw r;
            }
            if (e instanceof IOException ioException) {
                throw new UncheckedIOException(ioException);
            }

            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public T get() {
        return Objects.requireNonNull(getCurrent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(@Nullable T t) {
        this.supplier = () -> t;
    }

    /**
     * Sets the supplier and clears the content.
     * <p>
     * This means that the next time the content is requested,
     * the supplier {@link Supplier} will generate the new content.
     *
     * @param supplier the supplier that will set the value the next time a content is requested
     */
    public <E extends Throwable> void set(@NotNull ThrowingSupplier<T, E> supplier) {
        this.supplier = supplier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSet() {
        return supplier != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ifEmpty(@NotNull Runnable runnable) {
        T content = getCurrent();

        if (content == null) {
            runnable.run();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull T getOrSet(Supplier<@NotNull T> supplier) {
        T current = getCurrent();

        if (current == null) {
            T newValue = supplier.get();
            set(newValue);
            return newValue;
        } else {
            return current;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull IfPresentStage ifPresent(@NotNull Consumer<@NotNull T> presentConsumer) {
        T current = getCurrent();

        if (current != null) {
            presentConsumer.accept(current);
            return IfPresentStage.wasPresent();
        } else {
            return IfPresentStage.wasMissing();
        }
    }

    @Override
    public String toString() {
        return "LazyStatelessValue{" +
                "supplier=" + supplier + +
                '}';
    }
}
