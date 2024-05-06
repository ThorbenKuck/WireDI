package com.wiredi.runtime.async.state;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.async.AsyncLoader;
import com.wiredi.runtime.async.Barrier;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * A state implementation that allows for modifications.
 *
 * This class should not be exposed in public apis and limited in scope to the enclosing class.
 *
 * @param <T>
 * @see State
 * @see ReadOnlyState
 */
public class ModifiableState<T> extends AbstractState<T> {

    @NotNull
    private final Barrier setupBarrier;

    private static final Logging logger = Logging.getInstance(ModifiableState.class);

    public static <T> ModifiableState<T> of(T t) {
        return new ModifiableState<>(t);
    }

    public static <T> ModifiableState<T> empty() {
        return new ModifiableState<>();
    }

    public static <T> ModifiableState<T> of(ThrowingSupplier<T, ?> supplier) {
        ModifiableState<T> empty = new ModifiableState<>();
        AsyncLoader.load(supplier, empty::set, empty::markAsDirty);

        return empty;
    }

    public ModifiableState(@NotNull T t) {
        super(t);
        setupBarrier = Barrier.opened();
    }

    public ModifiableState() {
        setupBarrier = Barrier.closed();
    }

    @NotNull
    @Override
    public T get() {
        return setupBarrier.get(super::get);
    }

    @NotNull
    @Override
    public T get(@NotNull final Duration duration) {
        return setupBarrier.get(duration, super::get);
    }

    @Override
    public void awaitUntilSet() {
        setupBarrier.traverse();
        tryThrow();
    }

    @Override
    public void awaitUntilSet(@NotNull final Duration duration) {
        setupBarrier.traverse(duration);
        tryThrow();
    }

    public void set(@NotNull final T t) {
        if (this.value != null) {
            throw new IllegalStateException("The state cannot be updated");
        }
        if (this.error != null) {
            throw new IllegalStateException("The state is already dirty", error);
        }
        this.value = t;
        logger.debug(() -> "Opening barrier " + this);
        notifyCallbacks(t);
        setupBarrier.open();
    }

    public void markAsDirty(@NotNull Throwable throwable) {
        if (this.value != null) {
            throw new IllegalStateException("The state cannot be updated");
        }
        if (this.error != null) {
            throw new IllegalStateException("The state is already dirty", error);
        }
        doMarkAsDirty(throwable);
        setupBarrier.open();
    }

    public void markAsDirty(String message) {
        markAsDirty(new StateException(message));
    }

    public void clear() {
        super.clear();
        setupBarrier.close();
    }
}
