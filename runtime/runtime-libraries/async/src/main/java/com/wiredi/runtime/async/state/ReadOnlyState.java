package com.wiredi.runtime.async.state;

import org.jetbrains.annotations.Nullable;

/**
 * A read-only state implementation.
 * <p>
 * This class extends {@link AbstractState} and provides a simple, immutable state implementation.
 * It doesn't add any new methods or override any existing ones from AbstractState.
 * It's just a wrapper around a value that can't be modified.
 * <p>
 * ReadOnlyState is typically created using the {@link State#just(Object)} factory method.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Create a read-only state with a value
 * ReadOnlyState<String> state = State.just("Hello, World!");
 *
 * // Get the value
 * String value = state.get();
 * }</pre>
 * <p>
 * The usecase for this class is when the state is constructed at the same time as the class holding it and never changes.
 * For example, like this:
 *
 * <pre>{@code
 * public class MyStateFullComponent implements StateFull<String> {
 *
 *      @NotNull
 *      private final ReadOnlyState<String> state = State.just("Hello, World!");
 *
 *      @Override
 *      public @NotNull State<String> getState() {
 *          return this.state;
 *      }
 * }
 * }</pre>
 *
 * If the state is constructed at a later point in time, it's recommended to use the {@link ModifiableState}
 *
 * @param <T> the type of the value maintained in this state
 * @see State
 * @see AbstractState
 * @see ModifiableState
 */
public class ReadOnlyState<T> extends AbstractState<T> {

    public ReadOnlyState(@Nullable T value) {
        super(value);
    }
}
