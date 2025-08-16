package com.wiredi.runtime.async.state;

import com.wiredi.logging.Logging;
import com.wiredi.runtime.async.DataAccess;
import com.wiredi.runtime.lang.ThrowingConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A base class for implementing states.
 * <p>
 * This abstract class provides common functionality for state implementations, including:
 * <ul>
 *   <li>Maintaining a value and error state</li>
 *   <li>Implementing the get(), awaitUntilSet(), isSet(), and ifPresent() methods</li>
 *   <li>Managing callbacks for when the state is set</li>
 *   <li>Providing methods for clearing the state and checking if it's "dirty" (has an error)</li>
 * </ul>
 * <p>
 * Concrete implementations of this class should define how the state is set and how
 * synchronization is handled.
 *
 * @param <T> the type of the value maintained in this state
 * @see State
 * @see ModifiableState
 * @see ReadOnlyState
 */
public abstract class AbstractState<T> implements State<T> {

    private static final Logging logger = Logging.getInstance(AbstractState.class);
    private final DataAccess callbacksLock = new DataAccess();
    private final List<Consumer<T>> callbacks = new ArrayList<>();
    @Nullable
    protected Throwable error;
    @Nullable
    protected T value;

    public AbstractState(@Nullable T t) {
        this.value = t;
    }

    public AbstractState() {
        this(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StateOnSetSubscription onSet(Consumer<T> consumer) {
        callbacksLock.write(() -> callbacks.add(consumer));
        T current = value;
        if (current != null) {
            consumer.accept(current);
        }

        return () -> callbacksLock.write(() -> callbacks.remove(consumer));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull T get(@NotNull final Duration duration) {
        awaitUntilSet(duration);
        tryThrow();
        if (value == null) {
            throw new StateException("Tried to access a fixed state, but no fixed value was provided");
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull T get() {
        awaitUntilSet();
        tryThrow();
        if (value == null) {
            throw new StateException("Tried to access a fixed state, but no fixed value was provided");
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends Throwable> void ifPresent(@NotNull ThrowingConsumer<T, E> consumer) throws E, StateException {
        tryThrow();
        T t = this.value;

        if (t != null) {
            consumer.accept(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSet() {
        return this.value != this.error;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void awaitUntilSet() {
        // NoOp as the value is already present
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void awaitUntilSet(@NotNull Duration duration) {
        // NoOp as the value is already present
    }

    @Override
    public String toString() {
        String prefix = getClass().getSimpleName();
        List<String> values = new ArrayList<>();
        if (!isSet()) {
            prefix = "[Empty]" + prefix;
        }
        if (error != null) {
            values.add("value=" + value);
            if (error.getMessage() != null) {
                values.add("error=" + error.getMessage());
            } else {
                values.add("error=" + error.getClass().getSimpleName());
            }
        } else if (value != null) {
            values.add(value.toString());
        }
        return prefix + "(" +
                String.join(", ", values) +
                ')';
    }

    /**
     * Clears this states' value and error.
     * <p>
     * Will not do a check for throwing before doing so.
     */
    public void clear() {
        this.value = null;
        this.error = null;
    }

    public boolean isDirty() {
        return this.error != null;
    }

    /**
     * A function to notify all callbacks about a value.
     *
     * @param t the value to notify about
     */
    protected void notifyCallbacks(T t) {
        this.callbacksLock.read(() -> this.callbacks.forEach(c -> c.accept(t)));
    }

    protected void doMarkAsDirty(@NotNull Throwable throwable) {
        this.error = throwable;
    }

    protected void doMarkAsDirty(String message) {
        this.doMarkAsDirty(new StateException(message));
    }

    protected void tryThrow() throws StateException {
        if (this.error != null) {
            if (this.error instanceof StateException s) {
                throw s;
            } else if (this.error instanceof RuntimeException r) {
                throw r;
            } else {
                throw new StateException("Tried to access a dirty state", this.error);
            }
        }
    }
}
