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
 * <p>
 * It holds the content separately from the supplier.
 * If a content is requested from the value, it checks the following state:
 * <ol>
 *     <li>If the content is set, the content is returned.</li>
 *     <li>If the content but a Supplier is set, the supplier generates the content that is set and returned. Then the supplier is removed.</li>
 *     <li>If the content and Supplier is null, the value is considered empty.</li>
 * </ol>
 * <p>
 * This value provides additional functions ({@link #set(ThrowingSupplier)}), to set a {@link ThrowingSupplier}
 *
 * @param <T>
 */
public class LazyValue<T> implements Value<T> {

    @Nullable
    private ThrowingSupplier<T, ?> supplier;
    @Nullable
    private T content;

    public LazyValue(@NotNull ThrowingSupplier<@Nullable T, ?> supplier) {
        this.supplier = supplier;
    }

    /**
     * {@inheritDoc}
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
