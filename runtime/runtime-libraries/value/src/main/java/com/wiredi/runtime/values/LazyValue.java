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
 * A Value implementation that lazily initializes its content.
 * <p>
 * This value holds the content separately from the supplier that creates it.
 * When content is requested from the value, it follows this process:
 * <ol>
 *     <li>If the content is already set, the content is returned directly.</li>
 *     <li>If the content is null but a supplier is set, the supplier is used to generate the content,
 *         which is then stored and returned. The supplier is then removed to avoid repeated computation.</li>
 *     <li>If both the content and supplier are null, the value is considered empty.</li>
 * </ol>
 * <p>
 * This value provides an additional method {@link #set(ThrowingSupplier)} to set a supplier
 * that will be used to lazily initialize the content when needed.
 *
 * @param <T> The type of value stored in this container
 * @see Value#lazy(ThrowingSupplier)
 */
public class LazyValue<T> implements Value<T> {

    @Nullable
    private ThrowingSupplier<T, ?> supplier;
    @Nullable
    private T content;

    /**
     * Creates a new LazyValue with the specified supplier.
     * <p>
     * The supplier will be used to initialize the value when it is first accessed.
     * After the value is initialized, the supplier is discarded to avoid repeated computation.
     *
     * @param supplier The supplier that will provide the value when needed
     */
    public LazyValue(@NotNull ThrowingSupplier<@Nullable T, ?> supplier) {
        this.supplier = supplier;
    }

    /**
     * Gets the current value, initializing it if necessary.
     * <p>
     * This method handles the lazy initialization logic. If the content is not yet set
     * but a supplier is available, it will use the supplier to initialize the content.
     * Any exceptions thrown by the supplier are properly handled and rethrown.
     *
     * @return The current value, which may be null
     */
    @Nullable
    private T getCurrent() {
        if (content == null) {
            if (supplier != null) {
                try {
                    content = supplier.get();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Throwable e) {
                    if (e instanceof IOException) {
                        throw new UncheckedIOException((IOException) e);
                    }
                    throw new UndeclaredThrowableException(e);
                }
                supplier = null;
            }
        }

        return content;
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
        this.content = t;
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
        this.content = null;
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
        return "LazyValue{" +
                "content=" + content +
                '}';
    }
}
