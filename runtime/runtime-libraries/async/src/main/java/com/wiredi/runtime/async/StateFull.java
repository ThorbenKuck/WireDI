package com.wiredi.runtime.async;

import com.wiredi.runtime.async.state.State;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for classes that maintain a {@link State} object.
 * <p>
 * Classes implementing this interface are responsible for managing a state that can be accessed by other components.
 * The state is represented by a {@link State} object, which provides methods for retrieving the state value,
 * waiting for the state to be set, and checking if the state is set.
 * <p>
 * During application shutdown, the {@link #dismantleState()} method is called to clean up any resources associated with the state.
 * This method is called by the application lifecycle manager.
 * <p>
 * StateFull instances are automatically synchronized during application startup,
 * ensuring that all states are set before the application is considered ready.
 * <p>
 * Though loaded into the WireRepository, the repository isn't synchronizing the StateFull instances.
 * When used in combination with the WiredApplication though, all states are synchronized on startup.
 *
 * @param <T> the type of the value maintained in the state
 * @see State
 */
public interface StateFull<T> {

    /**
     * Returns the state maintained by this class.
     *
     * @return the state object
     */
    @NotNull
    State<T> getState();

    /**
     * Cleans up any resources associated with the state.
     * <p>
     * This method is called during application shutdown to ensure proper cleanup.
     * The default implementation does nothing.
     */
    default void dismantleState() {
    }
}
