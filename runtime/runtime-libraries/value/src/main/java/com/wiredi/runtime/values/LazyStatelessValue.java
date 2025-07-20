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
 * A Value implementation that always consults its supplier to retrieve the value.
 * <p>
 * Unlike {@link LazyValue} which caches the result of the supplier after the first call,
 * this implementation is stateless and always delegates to the underlying supplier
 * whenever the value is requested. This means the value can change over time if the
 * supplier returns different values on different calls.
 * <p>
 * This is useful when you need a value that can dynamically change based on some
 * external state, but still want to use the Value interface.
 *
 * @param <T> The type of value provided by this container
 * @see Value#of(ThrowingSupplier)
 * @see LazyValue
 */
public class LazyStatelessValue<T> implements Value<T> {

    @Nullable
    private ThrowingSupplier<T, ?> supplier;

    /**
     * Creates a new LazyStatelessValue with the specified supplier.
     * <p>
     * The supplier will be consulted every time the value is accessed.
     *
     * @param supplier The supplier that will provide the value when needed
     */
    public LazyStatelessValue(@NotNull ThrowingSupplier<@Nullable T, ?> supplier) {
        this.supplier = supplier;
    }

    /**
     * Gets the current value by consulting the supplier.
     * <p>
     * This method handles the delegation to the supplier and proper exception handling.
     * Any exceptions thrown by the supplier are properly handled and rethrown.
     *
     * @return The current value from the supplier, which may be null
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
                "supplier=" + supplier +
                '}';
    }
}
