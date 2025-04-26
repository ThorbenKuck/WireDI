package com.wiredi.runtime.async.state;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.async.AsyncLoader;
import com.wiredi.runtime.async.barriers.SemaphoreBarrier;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * A state implementation that allows for modifications.
 * <p>
 * This class extends {@link AbstractState} and provides methods for setting the state value
 * and marking it as dirty (with an error). It uses a {@link SemaphoreBarrier} for synchronization,
 * allowing threads to wait until the state is set.
 * <p>
 * ModifiableState is designed to be used internally by classes that need to maintain and update a state.
 * It shouldn't be exposed in public APIs and should be limited in scope to the enclosing class.
 * Instead, it should be exposed as a {@link State} interface to external classes.
 * <p>
 * Example usage:
 * <pre>{@code
 * public class MyClass {
 *     private final ModifiableState<MyStateValue> state = State.empty();
 *
 *     public void logic() {
 *          // Execute business logic
 *          state.set(new MyStateValue());
 *     }
 *
 *     public State<MyStateValue> state() {
 *          return this.state;
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of the value maintained in this state
 * @see State
 * @see ReadOnlyState
 * @see AbstractState
 */
public class ModifiableState<T> extends AbstractState<T> {

    @NotNull
    private final SemaphoreBarrier setupBarrier;

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
        setupBarrier = SemaphoreBarrier.opened();
    }

    public ModifiableState() {
        setupBarrier = SemaphoreBarrier.closed();
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
