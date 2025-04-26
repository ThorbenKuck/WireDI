package com.wiredi.runtime.async.state;

/**
 * Represents a subscription to a state's onSet event.
 * <p>
 * This interface is returned by the {@link State#onSet(java.util.function.Consumer)} method
 * and allows the caller to cancel the subscription when it's no longer needed.
 * <p>
 * Example usage:
 * <pre>{@code
 * // Subscribe to state changes
 * StateOnSetSubscription subscription = state.onSet(value -> {
 *     System.out.println("State value: " + value);
 * });
 * 
 * // Later, when the subscription is no longer needed
 * subscription.cancel();
 * }</pre>
 * 
 * @see State#onSet(java.util.function.Consumer)
 * @see State#sinkTo(ModifiableState)
 */
public interface StateOnSetSubscription {

    /**
     * Cancels this subscription.
     * <p>
     * After calling this method, the consumer associated with this subscription
     * will no longer be notified when the state's value is set.
     */
    void cancel();

}
