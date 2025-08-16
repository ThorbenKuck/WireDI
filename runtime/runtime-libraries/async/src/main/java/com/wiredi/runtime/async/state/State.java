package com.wiredi.runtime.async.state;

import com.wiredi.runtime.lang.ThrowingConsumer;
import com.wiredi.runtime.lang.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * This class is representing a special state maintained by a class.
 * <p>
 * A state is an eventually consistent container with utility functions to read it.
 * The class enclosing and maintaining the state is considered its owner.
 * If a class wants to maintain a certain state, it can use this state and update it whenever.
 *
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
 * <p>
 * This can then be used to (for example) await the business logic result.
 * To do so, other processes can use the synchronization methods like {@link #get()} or {@link #awaitUntilSet()}.
 * These methods wait until the state is set.
 *
 * <pre>{@code
 * public class MyOtherClass {
 *      private MyClass dependency; // Inject from somewhere
 *
 *      public void await() {
 *          dependency.state().awaitUntilSet();
 *      }
 * }
 * }</pre>
 * <p>
 * Most, if not all states in public apis should be read-only by design.
 * Externalized classes do not know the required business logic to construct a concrete state, and it would be an
 * inappropriate distribution of logic to do so.
 * <p>
 * Instead, consider keeping a {@link ModifiableState} internally and expose it externally as a {@link State}.
 * This way external classes can read the state, but not write to it.
 * <p>
 * Theoretically, it is possible for external applications to modify a modifiable state externally, by casting it.
 *
 * <pre>{@code
 * public class MyModifyingOtherClass {
 *      private MyClass dependency; // Inject from somewhere
 *
 *      public void update() {
 *          // Check if the state is modifiable
 *          ((ModifiableState<MyStateValue>) dependency.state()).set(new MyStateValue());
 *      }
 * }
 * }</pre>
 * <p>
 * Please note that this is not recommended.
 * As a state is maintained by its owner, modifying the state from outside the enclosing class would override the values
 * set by the owner of the state.
 * This could break how the owner is planning to handle the state and when to change this.
 * <p>
 * There is no hard enforcing of this rule for two reasons:
 * <ol>
 *     <li>Checking access to this class would require reflections, which we want to avoid.</li>
 *     <li>There might be cases where it is required to modify a state from outside, especially when custom state implementations are used.</li>
 * </ol>
 * Still, it should be used only in the rarest of cases.
 *
 * @param <T>
 */
public interface State<T> {

    /**
     * Creates a new {@code State}, that is just wrapping the provided {@code value}.
     * <p>
     * The resulting {@code State} is not modifiable and thereby read-only.
     *
     * @param value the value to wrap.
     * @param <T>   the generic of the value wrapped in the state.
     * @return a new, read-only state.
     */
    static <T> ReadOnlyState<T> just(@Nullable T value) {
        return new ReadOnlyState<>(value);
    }

    /**
     * Constructs a new, modifiable state.
     * <p>
     * The state is initiated with the {@code value}.
     *
     * @param value the value to wrap.
     * @param <T>   the generic of the value wrapped in the state.
     * @return a new, modifiable state.
     * @see ModifiableState#of(Object)
     */
    static <T> ModifiableState<T> of(@NotNull T value) {
        return ModifiableState.of(value);
    }

    /**
     * Constructs a new, modifiable state based on the {@code supplier}.
     * <p>
     * The state is initiated with the value returned by the Throwing supplier.
     * Most notably, the state returned is filled asynchronously through the {@link com.wiredi.runtime.async.AsyncLoader}.
     * This means that the returned state is initially empty and only eventually contains the returned value of the supplier.
     *
     * @param supplier the supplier that provides the value.
     * @param <T>      the generic of the value wrapped in the state.
     * @return a new, modifiable state that eventually is filled.
     * @see ModifiableState#of(ThrowingSupplier)
     */
    static <T> ModifiableState<T> of(@NotNull ThrowingSupplier<T, ?> supplier) {
        return ModifiableState.of(supplier);
    }

    /**
     * Returns a new, modifiable state with not initialized value.
     *
     * @param <T> the generic of the value wrapped in the state.
     * @return a new, modifiable state
     */
    static <T> ModifiableState<T> empty() {
        return ModifiableState.empty();
    }

    /**
     * Retrieves and returns the content of this state.
     * <p>
     * If empty, this method will wait indefinitely until the state is set.
     *
     * @return the content of this state.
     */
    @NotNull
    T get();

    /**
     * Retrieves and returns the content of this state.
     * <p>
     * If empty, this method will wait indefinitely for {@code duration}.
     *
     * @return the content of this state.
     */
    @NotNull
    T get(@NotNull Duration duration);

    /**
     * Waits indefinitely until the content of this state is set.
     */
    void awaitUntilSet();

    /**
     * Waits for up to {@code duration} until the content of this state is set.
     */
    void awaitUntilSet(@NotNull Duration duration);

    /**
     * If the state is set.
     *
     * @return true if the state is set, false otherwise.
     */
    boolean isSet();

    /**
     * Consumes the state if it is set.
     *
     * @param consumer the consumer to consume the state.
     */
    <E extends Throwable> void ifPresent(@NotNull ThrowingConsumer<@NotNull T, E> consumer) throws E;

    /**
     * Add a callback to be invoked once this states' value is set.
     * <p>
     * If the state already is set, implementations of this interface should directly invoke the consumer.
     *
     * @param consumer the consumer to invoke with the states' value
     */
    StateOnSetSubscription onSet(Consumer<T> consumer);

    /**
     * Sinks the state to another state.
     * <p>
     * With this operation, the provided state {@code other} will be updated whenever the state of this state changes.
     * There will be no inverse connection, i.e. this state will not be updated whenever the state of {@code other} changes.
     * <p>
     * This method returns a class to cut this connection.
     * Callers must make sure to maintain these subscriptions if they want to break this sink in the future.
     *
     * @param other the state that should be updated whenever the state of this state changes.
     * @return a subscription that can be used to unsubscribe.
     */
    default StateOnSetSubscription sinkTo(ModifiableState<? super T> other) {
        return onSet(other::set);
    }
}
